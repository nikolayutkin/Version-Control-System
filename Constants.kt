package svcs

enum class Constants (val path : String) {
     pathVCS ("vcs"),
     commitPath ("${pathVCS}/commits"),
     indexPath("${pathVCS}/index.txt"),
     logPath ("${pathVCS}/log.txt"),
     hashPath ("${pathVCS}/hash.txt"),
     addFileToIndex("Add a file to the index.")
}