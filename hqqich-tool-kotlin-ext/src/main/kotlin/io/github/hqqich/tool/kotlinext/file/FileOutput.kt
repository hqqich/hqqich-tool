package io.github.hqqich.tool.kotlinext.file

import io.github.hqqich.tool.kotlinext.ext.otherwise
import io.github.hqqich.tool.kotlinext.ext.yes
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter

/**
 * 文件输出类
 */
class FileOutput {

    /**
     * 写文件。
     */
    fun writeFile(content: StringBuilder, fileName: String) {

        // 在文件夹目录下新建文件
        val file = File(fileName)

        file.exists().yes {
            // 是个文件
        }.otherwise {
            // 不是文件，创建文件
            file.createNewFile()
        }



        OutputStreamWriter(FileOutputStream(file)).use {
            // 写入内容
            it.write(content.toString())
            // 换行
            it.write("\r\n")
        }

//        lateinit var fileOutputStream: FileOutputStream
//        lateinit var outputStreamWriter: OutputStreamWriter
//        try {
//            fileOutputStream = if (!file.exists()) {
//                val hasFile = file.createNewFile()
//                if (hasFile) {
////                    io.github.hqqich.oraclemysqlserver.util.FileOutput.logger.info("file not exists, create new file")
//                }
//                FileOutputStream(file)
//            } else {
//                FileOutputStream(file, true)
//            }
//            outputStreamWriter = OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)
//            // 写入内容
//            outputStreamWriter.write(content.toString())
//            // 换行
//            outputStreamWriter.write("\r\n")
//        } catch (_: Exception) {
//        } finally {
//            // 关闭流
//            try {
//                outputStreamWriter.close()
//                fileOutputStream.close()
//            } catch (_: IOException) {
//            }
//        }

    }


}
