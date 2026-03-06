// Exercise 1: Null-Safe Data Processing

// Data class with nullable email
data class User(val name: String, val email: String?)

fun main() {
    // Create list of users
    val users = listOf(
        User("Alex", "alex@example.com"),
        User("Blake", null),
        User("Casey", "casey@work.com")
    )

    // Requirement 1 & 2: Print emails in uppercase or message for no email
    println("=== User Email List ===")
    for (user in users) {
        val emailMessage = user.email?.let { email ->
            email.uppercase()
        } ?: "${user.name} has no email"
        println(emailMessage)
    }

    // Requirement 3: Count users with valid emails
    println("\n=== Email Statistics ===")
    val validEmailCount = users.count { user ->
        user.email != null
    }
    println("Users with valid emails: $validEmailCount out of ${users.size}")

    // Alternative approach using also() to print while processing
    println("\n=== Processing with also() ===")
    users.forEach { user ->
        user.email?.also { email ->
            println("${user.name} -> ${email.uppercase()}")
        } ?: println("${user.name} has no email")
    }
}
