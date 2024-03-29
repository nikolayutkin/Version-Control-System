package svcs

import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printHelp()
    } else {
        when (args.first().toString()) {
            KeyWord.help.name -> {
                printHelp()
            }
            KeyWord.config.name -> {
                check(args, ::config)
            }
            KeyWord.add.name -> {
                check(args, ::add)
            }
            KeyWord.log.name -> {
                log()
            }
            KeyWord.commit.name -> {
                check(args, ::commit)
            }
            KeyWord.checkout.name -> {
                check(args, ::checkout)
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


fun config(str: String = "") {
    val configPath = "${Constants.pathVCS.path}/config.txt"
    if (!File(configPath).exists()) {
        createVCSFolder()
        createFile(configPath)
    }
    if (str.isBlank()) {
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
    if (!File(Constants.indexPath.path).exists()) {
        createVCSFolder()
        createFile(Constants.indexPath.path)
    }
    if (str.isBlank()) {
        if (File(Constants.indexPath.path).readText().isBlank()) {
            println(Constants.addFileToIndex.path)
        } else {
            println("Tracked files:\n${File(Constants.indexPath.path).readText()}")
        }
    } else {
        if (File(str).exists()) {
            createFile(Constants.indexPath.path)
            File(Constants.indexPath.path).appendText("$str\n")
            println("The file '$str' is tracked.")
        } else {
            println("Can't find '$str'.")
        }
    }
}

fun commit(str: String) {
    createVCSFolder()
    createDir(Constants.commitPath.path)
    createFile(Constants.logPath.path)
    createFile(Constants.hashPath.path)
    if (str.isBlank()) {
        println("Message was not passed.")
    } else if (File(Constants.indexPath.path).exists()) {
        val fileHash = getFileHash()
        val author = getAuthor()
        if (author == "") {
            println("Please configure your name first.")
        } else {
            if (fileHash == File(Constants.hashPath.path).readText() && File(Constants.hashPath.path).readText().isNotEmpty()) {
                    println("Nothing to commit.")
                }
            else {
                addLog("commit $fileHash\nAuthor: $author\n${str.replace("[[/]]","")}\n")
                println("Changes are committed.")
                copyCommitFile(fileHash)
                File(Constants.hashPath.path).writeText(fileHash)
            }
        }
    } else {
        println("Nothing to commit.")
    }
}



fun log() {
    createVCSFolder()
    val logPath = "${Constants.pathVCS.path}/log.txt"
    createFile(logPath)
    if (File(logPath).readText().isBlank()) {
        println("No commits yet.")
    } else {
        println(File(logPath).readText())
    }
}

fun checkout(str: String) {
    if (str.isBlank()) {
        println("Commit id was not passed.")
    } else {
        val checkoutDir = File("${Constants.commitPath.path}/$str")
        if (checkoutDir.exists()) {
            val listOfFile = File(Constants.indexPath.path).readText().split("\n")
            val commitDir = "${Constants.commitPath.path}/${str}"
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

//----- System function -----//

fun createVCSFolder() {
    val dir = File(Constants.pathVCS.path)
    if (!dir.exists()) dir.mkdir()
}

fun createFile(name: String) {
    val file = File(name)
    if (!file.exists()) file.createNewFile()
}

fun createDir(name: String) {
    val dir = File(name)
    if (!dir.exists()) dir.mkdir()
}

fun getFileHash(): String {
    var allFilesText = ""
    val listOfFile = File(Constants.indexPath.path).readText().split("\n")
    listOfFile.forEach { file ->
        if (File(file).exists())
            allFilesText += File(file).readText()
    }
    return generateHash(allFilesText)
}

fun copyCommitFile(dir: String) {
    val listOfFile = File(Constants.indexPath.path).readText().split("\n")
    val commitDir = "${Constants.commitPath.path}/$dir"
    createDir(commitDir)
    listOfFile.forEach { file ->
        if (File(file).exists())
            File(file).copyTo(File("$commitDir/$file"))
    }
}

fun getAuthor(): String {
    val config = File("${Constants.pathVCS.path}/config.txt")
    return if (config.exists() && config.readText().isNotBlank()) {
        config.readText()
    } else {
        ""
    }
}

fun addLog(str: String) {
    val logFile = File(Constants.logPath.path)
    val oldLog = logFile.readText()
    logFile.writeText(str)
    logFile.appendText("\n$oldLog")
}

fun generateHash(str: String): String {
    return str.hashCode().toString()
}

fun check(input: Array<String>, func: (String) -> Unit) {
    if (input.size == 1) {
        func("")
    } else {
        func(input.drop(0).toString())
    }
}