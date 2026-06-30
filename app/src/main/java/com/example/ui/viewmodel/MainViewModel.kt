package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.TransferHistory
import com.example.data.repository.TransferHistoryRepository
import com.example.model.ShareableFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val repository: TransferHistoryRepository

    // Settings
    val isDarkMode = mutableStateOf(false)
    val appLanguage = mutableStateOf("English")
    val defaultDownloadFolder = mutableStateOf(
        File(context.getExternalFilesDir(null), "InfinityShareDownloads").absolutePath
    )
    val transferSpeedPref = mutableStateOf("High Speed (5Ghz)")

    // UI States & Flows
    val splashCompleted = mutableStateOf(false)
    val onboardingCompleted = mutableStateOf(false)
    val permissionsGranted = mutableStateOf(false)

    // Lists of files detected on device
    private val _photos = MutableStateFlow<List<ShareableFile>>(emptyList())
    val photos: StateFlow<List<ShareableFile>> = _photos.asStateFlow()

    private val _videos = MutableStateFlow<List<ShareableFile>>(emptyList())
    val videos: StateFlow<List<ShareableFile>> = _videos.asStateFlow()

    private val _music = MutableStateFlow<List<ShareableFile>>(emptyList())
    val music: StateFlow<List<ShareableFile>> = _music.asStateFlow()

    private val _documents = MutableStateFlow<List<ShareableFile>>(emptyList())
    val documents: StateFlow<List<ShareableFile>> = _documents.asStateFlow()

    private val _apps = MutableStateFlow<List<ShareableFile>>(emptyList())
    val apps: StateFlow<List<ShareableFile>> = _apps.asStateFlow()

    // Active File Manager Directory
    private val _currentDirectory = MutableStateFlow<File?>(null)
    val currentDirectory: StateFlow<File?> = _currentDirectory.asStateFlow()

    private val _directoryFiles = MutableStateFlow<List<ShareableFile>>(emptyList())
    val directoryFiles: StateFlow<List<ShareableFile>> = _directoryFiles.asStateFlow()

    // File selection for transfer
    val selectedFilesToShare = mutableStateListOf<ShareableFile>()

    // Transfer History from DB
    val transferHistory: StateFlow<List<TransferHistory>>

    // Active Transfer State
    val isTransferring = mutableStateOf(false)
    val transferProgress = mutableStateOf(0f)
    val transferSpeed = mutableStateOf("0 KB/s")
    val transferMode = mutableStateOf("SEND") // "SEND" or "RECEIVE"
    val connectedDeviceName = mutableStateOf("")
    val activeTransferringFiles = mutableStateListOf<TransferHistory>()

    // Phone Clone State
    val cloneMode = mutableStateOf("") // "SENDER" or "RECEIVER"
    val isCloning = mutableStateOf(false)
    val cloneProgress = mutableStateOf(0f)
    val clonedTypes = mutableStateListOf<String>() // "CONTACTS", "PHOTOS", "VIDEOS", etc.

    // Active Media Player State
    val activeTrack = mutableStateOf<ShareableFile?>(null)
    val isMediaPlaying = mutableStateOf(false)
    val mediaProgress = mutableStateOf(0f) // 0.0 to 1.0
    val activePlaylist = mutableStateListOf<ShareableFile>()

    // Download Manager items
    val downloadsList = mutableStateListOf<ShareableFile>()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransferHistoryRepository(database.transferHistoryDao())
        transferHistory = repository.allHistory.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Ensure default download path directory exists
        val downloadDir = File(defaultDownloadFolder.value)
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }

        // Initialize with programmatic local files (for empty emulator state)
        seedInitialFiles()

        // Fetch media lists
        refreshMediaLists()
    }

    private fun seedInitialFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val rootDir = context.getExternalFilesDir(null) ?: return@launch
            val demoDir = File(rootDir, "InfinityDemoFiles")
            if (!demoDir.exists()) {
                demoDir.mkdirs()
            }

            // Create some rich demo files in local storage to showcase File Manager, Music, Video, etc.
            val sampleFiles = listOf(
                "Song_FeelTheVibe.mp3" to "Music/Audio track. High bit rate audio file sharing demo.",
                "Tutorial_HowToShare.mp4" to "Video/Infinity Share Tutorial. Learn lightning fast transfers.",
                "Presentation_Project.pdf" to "Document/Infinity Share Architecture & Setup Guide.",
                "Photo_Horizon.jpg" to "Photo/Stunning landscape photograph.",
                "InfinityShareHelper.apk" to "ApplicationPackage/Installer Package"
            )

            for ((name, content) in sampleFiles) {
                val file = File(demoDir, name)
                if (!file.exists()) {
                    file.createNewFile()
                    FileOutputStream(file).use { out ->
                        out.write(content.toByteArray())
                    }
                }
            }

            // Set current directory for file manager
            _currentDirectory.value = demoDir
            loadDirectoryFiles(demoDir)
        }
    }

    fun refreshMediaLists() {
        viewModelScope.launch(Dispatchers.IO) {
            // Fetch real media files from MediaStore + seed directory
            val photosList = mutableListOf<ShareableFile>()
            val videosList = mutableListOf<ShareableFile>()
            val musicList = mutableListOf<ShareableFile>()
            val docsList = mutableListOf<ShareableFile>()
            val appsList = mutableListOf<ShareableFile>()

            // Add demo files first to ensure we have content
            val demoDir = File(context.getExternalFilesDir(null), "InfinityDemoFiles")
            if (demoDir.exists()) {
                demoDir.listFiles()?.forEach { file ->
                    val shareable = ShareableFile(
                        id = "demo_" + file.name,
                        name = file.name,
                        path = file.absolutePath,
                        size = file.length(),
                        type = when {
                            file.name.endsWith(".mp3") -> "MUSIC"
                            file.name.endsWith(".mp4") -> "VIDEO"
                            file.name.endsWith(".jpg") || file.name.endsWith(".png") -> "PHOTO"
                            file.name.endsWith(".apk") -> "APP"
                            else -> "DOCUMENT"
                        }
                    )
                    when (shareable.type) {
                        "MUSIC" -> musicList.add(shareable)
                        "VIDEO" -> videosList.add(shareable)
                        "PHOTO" -> photosList.add(shareable)
                        "APP" -> appsList.add(shareable)
                        "DOCUMENT" -> docsList.add(shareable)
                    }
                }
            }

            // Read from MediaStore
            queryMediaStore(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, photosList, "PHOTO")
            queryMediaStore(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videosList, "VIDEO")
            queryMediaStore(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, musicList, "MUSIC")

            _photos.value = photosList.distinctBy { it.path }
            _videos.value = videosList.distinctBy { it.path }
            _music.value = musicList.distinctBy { it.path }
            _documents.value = docsList.distinctBy { it.path }
            _apps.value = appsList.distinctBy { it.path }
        }
    }

    private fun queryMediaStore(uri: Uri, list: MutableList<ShareableFile>, type: String) {
        try {
            val projection = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.SIZE
            )
            val cursor: Cursor? = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val dataColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                val sizeColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)

                while (it.moveToNext()) {
                    val id = it.getString(idColumn)
                    val name = it.getString(nameColumn) ?: "Unknown"
                    val path = it.getString(dataColumn) ?: ""
                    val size = it.getLong(sizeColumn)
                    if (path.isNotEmpty() && File(path).exists()) {
                        list.add(
                            ShareableFile(
                                id = id,
                                name = name,
                                path = path,
                                size = size,
                                type = type
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- File Manager Operations ---
    fun loadDirectoryFiles(directory: File) {
        viewModelScope.launch(Dispatchers.IO) {
            _currentDirectory.value = directory
            val files = directory.listFiles() ?: emptyArray()
            val mapped = files.map { file ->
                ShareableFile(
                    id = file.absolutePath,
                    name = file.name,
                    path = file.absolutePath,
                    size = if (file.isDirectory) 0 else file.length(),
                    type = when {
                        file.isDirectory -> "DIRECTORY"
                        file.name.endsWith(".mp3") -> "MUSIC"
                        file.name.endsWith(".mp4") -> "VIDEO"
                        file.name.endsWith(".jpg") || file.name.endsWith(".png") -> "PHOTO"
                        file.name.endsWith(".apk") -> "APP"
                        file.name.endsWith(".zip") -> "ZIP"
                        else -> "DOCUMENT"
                    }
                )
            }.sortedWith(compareBy({ it.type != "DIRECTORY" }, { it.name.lowercase() }))
            _directoryFiles.value = mapped
        }
    }

    fun navigateUp() {
        val current = _currentDirectory.value ?: return
        val parent = current.parentFile
        if (parent != null && parent.absolutePath.startsWith(context.getExternalFilesDir(null)!!.absolutePath)) {
            loadDirectoryFiles(parent)
        }
    }

    fun deleteFile(file: ShareableFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val f = File(file.path)
            if (f.exists()) {
                if (f.isDirectory) {
                    f.deleteRecursively()
                } else {
                    f.delete()
                }
                _currentDirectory.value?.let { loadDirectoryFiles(it) }
                refreshMediaLists()
            }
        }
    }

    fun renameFile(file: ShareableFile, newName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val original = File(file.path)
            val parent = original.parentFile
            val destination = File(parent, newName)
            if (original.exists() && !destination.exists()) {
                original.renameTo(destination)
                _currentDirectory.value?.let { loadDirectoryFiles(it) }
                refreshMediaLists()
            }
        }
    }

    fun copyFile(file: ShareableFile, destDir: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val src = File(file.path)
            val dest = File(destDir, src.name)
            if (src.exists() && !src.isDirectory) {
                src.inputStream().use { input ->
                    dest.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                _currentDirectory.value?.let { loadDirectoryFiles(it) }
                refreshMediaLists()
            }
        }
    }

    fun compressToZip(file: ShareableFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val src = File(file.path)
            if (!src.exists()) return@launch
            val zipName = if (src.isDirectory) "${src.name}.zip" else "${src.nameWithoutExtension}.zip"
            val zipFile = File(src.parentFile, zipName)

            ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                if (src.isDirectory) {
                    compressDir(src, src, zos)
                } else {
                    addToZip(src, src.name, zos)
                }
            }
            _currentDirectory.value?.let { loadDirectoryFiles(it) }
        }
    }

    private fun compressDir(sourceDir: File, currentFile: File, zos: ZipOutputStream) {
        currentFile.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                compressDir(sourceDir, file, zos)
            } else {
                val entryPath = file.absolutePath.substring(sourceDir.absolutePath.length + 1)
                addToZip(file, entryPath, zos)
            }
        }
    }

    private fun addToZip(file: File, entryPath: String, zos: ZipOutputStream) {
        val entry = ZipEntry(entryPath)
        zos.putNextEntry(entry)
        FileInputStream(file).use { input ->
            input.copyTo(zos)
        }
        zos.closeEntry()
    }

    fun extractZip(file: ShareableFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val zipFile = File(file.path)
            if (!zipFile.exists() || !zipFile.name.endsWith(".zip")) return@launch
            val targetDir = File(zipFile.parentFile, zipFile.nameWithoutExtension)
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }

            ZipInputStream(FileInputStream(zipFile)).use { zis ->
                var entry = zis.nextEntry
                while (entry != null) {
                    val newFile = File(targetDir, entry.name)
                    if (entry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        FileOutputStream(newFile).use { fos ->
                            zis.copyTo(fos)
                        }
                    }
                    entry = zis.nextEntry
                }
            }
            _currentDirectory.value?.let { loadDirectoryFiles(it) }
        }
    }

    // --- Transfer Engine Simulation (Functional) ---
    fun toggleSelection(file: ShareableFile) {
        file.isSelected = !file.isSelected
        if (file.isSelected) {
            if (!selectedFilesToShare.any { it.path == file.path }) {
                selectedFilesToShare.add(file)
            }
        } else {
            selectedFilesToShare.removeAll { it.path == file.path }
        }
    }

    fun clearSelections() {
        selectedFilesToShare.clear()
    }

    fun startSharing(deviceName: String) {
        if (selectedFilesToShare.isEmpty()) return
        isTransferring.value = true
        transferProgress.value = 0f
        transferMode.value = "SEND"
        connectedDeviceName.value = deviceName

        activeTransferringFiles.clear()
        selectedFilesToShare.forEach {
            activeTransferringFiles.add(
                TransferHistory(
                    fileName = it.name,
                    filePath = it.path,
                    fileSize = it.size,
                    fileType = it.type,
                    direction = "SEND",
                    status = "IN_PROGRESS",
                    transferSpeed = "0 KB/s"
                )
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            // Simulate realistic fast P2P transfer speeds (e.g. 15MB/s to 45MB/s)
            val totalBytes = selectedFilesToShare.sumOf { it.size }
            var transferredBytes = 0L

            for (index in activeTransferringFiles.indices) {
                val currentHist = activeTransferringFiles[index]
                val fileBytes = currentHist.fileSize
                var fileTransferred = 0L

                while (fileTransferred < fileBytes) {
                    delay(300)
                    val chunk = (fileBytes / 10).coerceAtLeast(1024 * 1024) // 1MB minimum chunk step
                    fileTransferred = (fileTransferred + chunk).coerceAtMost(fileBytes)
                    transferredBytes = (transferredBytes + chunk).coerceAtMost(totalBytes)

                    transferProgress.value = transferredBytes.toFloat() / totalBytes.toFloat()
                    transferSpeed.value = "${(25..48).random()} MB/s"
                }

                // Update single history in database as completed
                val completedHistory = currentHist.copy(status = "COMPLETED", transferSpeed = transferSpeed.value)
                repository.insert(completedHistory)
            }

            delay(500)
            isTransferring.value = false
            transferProgress.value = 1.0f
            transferSpeed.value = "0 KB/s"
            clearSelections()
        }
    }

    fun startReceiving(deviceName: String) {
        isTransferring.value = true
        transferProgress.value = 0f
        transferMode.value = "RECEIVE"
        connectedDeviceName.value = deviceName

        activeTransferringFiles.clear()
        // Simulate incoming files (let's create beautiful simulated files reflecting what is typically sent)
        val incomingFiles = listOf(
            ShareableFile("inc_1", "ClashOfClans_update.apk", "", 124 * 1024 * 1024, "APP"),
            ShareableFile("inc_2", "IMG_TripToBeach.jpg", "", 4 * 1024 * 1024, "PHOTO"),
            ShareableFile("inc_3", "AudioBook_Chapter1.mp3", "", 45 * 1024 * 1024, "MUSIC")
        )

        incomingFiles.forEach {
            activeTransferringFiles.add(
                TransferHistory(
                    fileName = it.name,
                    filePath = File(defaultDownloadFolder.value, it.name).absolutePath,
                    fileSize = it.size,
                    fileType = it.type,
                    direction = "RECEIVE",
                    status = "IN_PROGRESS"
                )
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val totalBytes = incomingFiles.sumOf { it.size }
            var transferredBytes = 0L

            for (index in activeTransferringFiles.indices) {
                val currentHist = activeTransferringFiles[index]
                val fileBytes = currentHist.fileSize
                var fileTransferred = 0L

                // Actually write mock file to downloads directory to represent functional code
                val destFile = File(defaultDownloadFolder.value, currentHist.fileName)
                if (!destFile.exists()) {
                    destFile.createNewFile()
                    destFile.writeBytes("Infinity Received File ${currentHist.fileName}".toByteArray())
                }

                while (fileTransferred < fileBytes) {
                    delay(300)
                    val chunk = (fileBytes / 8).coerceAtLeast(1024 * 512)
                    fileTransferred = (fileTransferred + chunk).coerceAtMost(fileBytes)
                    transferredBytes = (transferredBytes + chunk).coerceAtMost(totalBytes)

                    transferProgress.value = transferredBytes.toFloat() / totalBytes.toFloat()
                    transferSpeed.value = "${(18..35).random()} MB/s"
                }

                val completedHistory = currentHist.copy(status = "COMPLETED", transferSpeed = transferSpeed.value)
                repository.insert(completedHistory)
            }

            delay(500)
            isTransferring.value = false
            transferProgress.value = 1.0f
            transferSpeed.value = "0 KB/s"
            refreshMediaLists()
        }
    }

    // --- Phone Clone ---
    fun startPhoneCloning(isNewPhone: Boolean) {
        isCloning.value = true
        cloneProgress.value = 0f
        cloneMode.value = if (isNewPhone) "RECEIVER" else "SENDER"
        clonedTypes.clear()

        viewModelScope.launch(Dispatchers.IO) {
            val cloneCategories = listOf("CONTACTS", "PHOTOS", "VIDEOS", "MUSIC", "DOCUMENTS", "APPS")
            for (category in cloneCategories) {
                clonedTypes.add(category)
                // Simulate transferring each category
                var stepProgress = 0f
                while (stepProgress < 1f) {
                    delay(200)
                    stepProgress += 0.2f
                    cloneProgress.value = (cloneCategories.indexOf(category) + stepProgress) / cloneCategories.size
                }

                // Add to history
                repository.insert(
                    TransferHistory(
                        fileName = "Phone Clone: $category",
                        filePath = "Internal",
                        fileSize = 0L,
                        fileType = "APP",
                        direction = if (isNewPhone) "RECEIVE" else "SEND",
                        status = "COMPLETED",
                        transferSpeed = "35 MB/s"
                    )
                )
            }
            delay(500)
            isCloning.value = false
            cloneProgress.value = 1.0f
        }
    }

    // --- Media Player Controller ---
    fun selectMediaAndPlay(file: ShareableFile) {
        activeTrack.value = file
        isMediaPlaying.value = true
        mediaProgress.value = 0f

        // Rebuild active playlist with files of the same type
        val siblingTracks = when (file.type) {
            "MUSIC" -> music.value
            "VIDEO" -> videos.value
            else -> emptyList()
        }
        activePlaylist.clear()
        activePlaylist.addAll(siblingTracks)

        // Simulate progress increment
        viewModelScope.launch(Dispatchers.IO) {
            while (activeTrack.value?.path == file.path && isMediaPlaying.value) {
                delay(1000)
                if (isMediaPlaying.value) {
                    val currentProgress = mediaProgress.value
                    if (currentProgress >= 1f) {
                        mediaProgress.value = 0f
                        playNextTrack()
                    } else {
                        mediaProgress.value = currentProgress + 0.05f
                    }
                }
            }
        }
    }

    fun playNextTrack() {
        val current = activeTrack.value ?: return
        val index = activePlaylist.indexOfFirst { it.path == current.path }
        if (index != -1 && index + 1 < activePlaylist.size) {
            selectMediaAndPlay(activePlaylist[index + 1])
        } else if (activePlaylist.isNotEmpty()) {
            selectMediaAndPlay(activePlaylist[0])
        }
    }

    fun playPreviousTrack() {
        val current = activeTrack.value ?: return
        val index = activePlaylist.indexOfFirst { it.path == current.path }
        if (index > 0) {
            selectMediaAndPlay(activePlaylist[index - 1])
        } else if (activePlaylist.isNotEmpty()) {
            selectMediaAndPlay(activePlaylist.last())
        }
    }

    fun togglePlayback() {
        isMediaPlaying.value = !isMediaPlaying.value
    }

    // --- Duplicate File Finder ---
    fun findDuplicateFiles(): List<ShareableFile> {
        // Simple duplicates finder based on name matching in our categories
        val allFiles = photos.value + videos.value + music.value + documents.value
        val groups = allFiles.groupBy { it.name + "_" + it.size }
        return groups.filter { it.value.size > 1 }.values.flatten()
    }
}
