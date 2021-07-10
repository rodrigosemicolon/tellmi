package com.example.tellmi.data

import com.example.tellmi.data.model.LoggedInUser
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {
    val test_password = "password123"
    val test_users = listOf("criancapt@gmail.com","adultopt@gmail.com","englishadult@gmail.com","frenchadult@gmail.com")

    fun login(username: String, password: String): Result<LoggedInUser> {
        try {
            // TODO: handle loggedInUser authentication
            if (username in test_users && password==test_password){
                val fakeUser = LoggedInUser(java.util.UUID.randomUUID().toString(), username)
                return Result.Success(fakeUser)
            }


            else{
                return Result.Error(IOException("Invalid username/password"))
            }


        } catch (e: Throwable) {
            return Result.Error(IOException("Error logging in", e))
        }
    }

    fun logout() {
        // TODO: revoke authentication
    }
}