package svcs

enum class KeyWord(name: String) {
    help("--help"),
    config("config"),
    add("add"),
    log("log"),
    commit("commit"),
    checkout("checkout")
}