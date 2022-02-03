package dev.ithurts.domain.debt

data class Binding(
    var filePath: String,
    var startLine: Int,
    var endLine: Int,
    val advancedBinding: AdvancedBinding?
) {
    val id: String? = null

    fun isAdvanced(): Boolean {
        return advancedBinding != null
    }

    fun update(filePath: String, startLine: Int, endLine: Int) {
        this.filePath = filePath
        this.startLine = startLine
        this.endLine = if (startLine < endLine) {
            endLine
        } else {
//            log.error("startLine < endLine for binding $id")
            startLine
        }
    }

    companion object {
        val log = org.slf4j.LoggerFactory.getLogger(Binding::class.java)
    }
}