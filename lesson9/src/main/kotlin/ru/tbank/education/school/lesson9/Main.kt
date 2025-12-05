import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

fun zipDirectoryWithFilter(
    sourceDirPath: String,
    zipPath: String,
    allowedExtensions: Set<String> = setOf("txt", "log")
) {
    val sourceDir = File(sourceDirPath)
    if (!sourceDir.exists() || !sourceDir.isDirectory) {
        println("Ошибка: источник '$sourceDirPath' не существует или не является каталогом.")
        return
    }

    val zipFile = File(zipPath)
    zipFile.parentFile?.let { parent ->
        if (!parent.exists()) {
            if (!parent.mkdirs()) {
                println("Не удалось создать родительские директории для архива: ${parent.path}")
                return
            }
        }
    }

    val filesToZip = sourceDir.walkTopDown()
        .filter { it.isFile && allowedExtensions.contains(it.extension.lowercase()) }
        .toList()

    if (filesToZip.isEmpty()) {
        println("Нет файлов с расширениями $allowedExtensions в каталоге ${sourceDir.path}. Создаю пустой архив.")
    }

    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)

    try {
        FileOutputStream(zipFile).use { fos ->
            ZipOutputStream(BufferedOutputStream(fos)).use { zos ->
                for (file in filesToZip) {
                    val relPath = sourceDir.toPath().relativize(file.toPath()).toString().replace(File.separatorChar, '/')
                    val entry = ZipEntry(relPath)
                    entry.size = file.length()
                    entry.time = file.lastModified()
                    zos.putNextEntry(entry)

                    FileInputStream(file).use { fis ->
                        BufferedInputStream(fis).use { bis ->
                            var len: Int
                            while (true) {
                                len = bis.read(buffer)
                                if (len <= 0) break
                                zos.write(buffer, 0, len)
                            }
                        }
                    }

                    zos.closeEntry()

                    println("Добавлено: $relPath  (${file.length()} байт)")
                }
            }
        }
        println("Архив создан: ${zipFile.path}")
    } catch (e: FileNotFoundException) {
        println("Ошибка: файл не найден - ${e.message}")
    } catch (e: IOException) {
        println("I/O ошибка при создании архива: ${e.message}")
    } catch (e: Exception) {
        println("Неожиданная ошибка: ${e.message}")
    }
}

fun main() {
    zipDirectoryWithFilter("project_data", "archive.zip", setOf("txt", "log"))
}
