package svcs

import java.io.File

//const val path = "/home/npc/IdeaProjects/Version Control System/Version Control System/task/src/svcs"

//const val pathVCS = "${path}/vcs"
const val pathVCS = "vcs"
const val commitPath = "${pathVCS}/commits"
const val indexPath = "${pathVCS}/index.txt"
const val logPath = "${pathVCS}/log.txt"
const val hashPath = "${pathVCS}/hash.txt"

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printHelp()
    } else {
        when (args.first().toString()) {
            "--help" -> {
                printHelp()
            }
            "config" -> {
                checkArray(args, ::config)
            }
            "add" -> {
                checkArray(args, ::add)
            }
            "log" -> {
                log()
            }
            "commit" -> {
                checkArray(args, ::commit)
            }
            "checkout" -> {
                checkArray(args, ::checkout)
            }
            else -> println("'${args.joinToString()}' is not a SVCS command.")
        }
    }
}
fun printHelp() {
    println(
        "These are SVCS commands:\n" +
                "config     Get and set a username.\n" +
                "add        Add a file to the index.\n" +
                "log        Show commit logs.\n" +
                "commit     Save changes.\n" +
                "checkout   Restore a file."
    )
}

fun checkArray(array: Array<String>, func: (String) -> Unit) {
    if (array.size == 1) {
        func("")
    } else {
        func(array.slice(1..array.lastIndex).joinToString())
    }
}

fun createVCSFolder() {
    val dir = File(pathVCS)
    if (!dir.exists()) dir.mkdir()
}

fun createFile(name: String) {
    val file = File(name)
    if(!file.exists()) file.createNewFile()
}

fun createDir(name: String) {
    val dir = File(name)
    if(!dir.exists()) dir.mkdir()
}

fun config(str: String = "") {
    val configPath = "${pathVCS}/config.txt"
    if (!File(configPath).exists()) {
        createVCSFolder()
        createFile(configPath)
    }
    if (str == "") {
        if (File(configPath).readText().isBlank()) {
            println("Please, tell me who you are.")
        } else {
            val name = File(configPath).readText()
            println("The username is $name.")
        }
    } else {
        File(configPath).writeText(str)
        println("The username is $str.")
    }
}

fun add(str: String = "") {
    if (!File(indexPath).exists()) {
        createVCSFolder()
        createFile(indexPath)
    }
    if (str == "") {
        if (File(indexPath).readText().isBlank()) {
            println("Add a file to the index.")
        } else {
            println("Tracked files:\n${File(indexPath).readText()}")
        }
    } else {
        if (File(str).exists()) {//("${path}/$str").exists()) {
            createFile(indexPath)
            File(indexPath).appendText("$str\n")
            println("The file '$str' is tracked.")
        } else {
            println("Can't find '$str'.")
        }
    }
}

fun commit(str: String) {
    createVCSFolder()
    createDir(commitPath)
    createFile(logPath)
    createFile(hashPath)
    if (str == "") {
        println("Message was not passed.")
    } else if (File(indexPath).exists()) {
        //val list = str.replace("[[/]]", "").split(" ")
        val fileHash = getFileHash()
        val author = getAuthor()
        if (author == "") {
            println("Please configure your name first.")
        } else {
            if (fileHash == File(hashPath).readText() && File(hashPath).readText().isNotEmpty()) {
                    println("Nothing to commit.")
                }
            else {
                addLog("commit $fileHash\nAuthor: $author\n${str.replace("[[/]]","")}\n")
                println("Changes are committed.")
                copyCommitFile(fileHash)
                File(hashPath).writeText(fileHash)
            }
        }
    } else {
        println("Nothing to commit.")
    }
}


fun getFileHash(): String {
    var allFilesText = ""
    //if (File(hashPath).readText().isNotBlank()) {
        val listOfFile = File(indexPath).readText().split("\n")
        listOfFile.forEach { file ->
            if(File(file).exists())
                allFilesText += File(file).readText()
        }
    return generateHash(allFilesText)
}

fun copyCommitFile(dir: String) {
    val listOfFile = File(indexPath).readText().split("\n")
    val commitDir = "$commitPath/$dir"
    createDir(commitDir)
    listOfFile.forEach { file ->
    if(File(file).exists())
        File(file).copyTo(File("$commitDir/$file"))
    }
}

fun getAuthor(): String {
    val config = File("${pathVCS}/config.txt")
    return if (config.exists() && config.readText().isNotBlank()) {
        config.readText()
    } else {
        ""
    }
}

fun addLog(str: String) {
    val logFile = File(logPath)
    val oldLog = logFile.readText()
    logFile.writeText(str)
    logFile.appendText("\n$oldLog")
}

fun generateHash(str: String): String {
    return str.hashCode().toString()
}

fun log() {
    createVCSFolder()
    val logPath = "${pathVCS}/log.txt"
    createFile(logPath)
    if (File(logPath).readText().isBlank()) {
        println("No commits yet.")
    } else {
        println(File(logPath).readText())
    }
}

fun checkout(str: String) {
    if (str == "") {
        println("Commit id was not passed.")
    } else {
        val checkoutDir = File("$commitPath/$str")
        if (checkoutDir.exists()) {
            val listOfFile = File(indexPath).readText().split("\n")
            val commitDir = "$commitPath/${str}"
            listOfFile.forEach { file ->
                if (File("$file").exists()) {
                    File("$commitDir/$file").copyTo(File("${file}"), overwrite = true) //copy file with overwrite
                }

            }
            println("Switched to commit $str.")
        } else {
            println("Commit does not exist.")
        }
    }
}
