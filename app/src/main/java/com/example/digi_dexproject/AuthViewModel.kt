package com.example.digi_dexproject

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Sealed class for Auth results (Login/Register)
sealed class AuthEvent {
    data class Success(val username: String) : AuthEvent()
    data class Failure(val message: String) : AuthEvent()
}

// Sealed class for Password Change results
sealed class PasswordChangeEvent {
    data class Success(val message: String) : PasswordChangeEvent()
    data class Failure(val message: String) : PasswordChangeEvent()
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao = UserDatabase.getDatabase(application).userDao()

    // Flow for Auth events
    private val _authResult = MutableSharedFlow<AuthEvent>()
    val authResult = _authResult.asSharedFlow()

    // Flow for Password Change events
    private val _passwordChangeResult = MutableSharedFlow<PasswordChangeEvent>()
    val passwordChangeResult = _passwordChangeResult.asSharedFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            if (username.isBlank() || password.isBlank()) {
                _authResult.emit(AuthEvent.Failure("Username and password cannot be empty."))
                return@launch
            }

            val user = withContext(Dispatchers.IO) {
                userDao.getUserByUsername(username)
            }

            if (user == null) {
                _authResult.emit(AuthEvent.Failure("Invalid username or password."))
            } else if (PasswordHasher.verify(password, user.passwordHash)) {
                _authResult.emit(AuthEvent.Success(user.username))
            } else {
                _authResult.emit(AuthEvent.Failure("Invalid username or password."))
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            if (username.isBlank() || password.isBlank()) {
                _authResult.emit(AuthEvent.Failure("Username and password cannot be empty."))
                return@launch
            }

            if (password.length < 4) { // Simple validation example
                _authResult.emit(AuthEvent.Failure("Password must be at least 4 characters."))
                return@launch
            }

            try {
                val hash = PasswordHasher.hash(password)
                val newUser = User(username, hash)

                withContext(Dispatchers.IO) {
                    userDao.registerUser(newUser)
                }

                // Automatically log in after successful registration
                _authResult.emit(AuthEvent.Success(newUser.username))
            } catch (e: Exception) {
                // This will catch if the username already exists (due to OnConflictStrategy.ABORT)
                _authResult.emit(AuthEvent.Failure("Username already exists."))
            }
        }
    }

    fun changePassword(username: String, oldPass: String, newPass: String) {
        viewModelScope.launch {
            if (oldPass.isBlank() || newPass.isBlank()) {
                _passwordChangeResult.emit(PasswordChangeEvent.Failure("Passwords cannot be empty."))
                return@launch
            }

            if (newPass.length < 4) { // Simple validation
                _passwordChangeResult.emit(PasswordChangeEvent.Failure("New password must be at least 4 characters."))
                return@launch
            }

            if (oldPass == newPass) {
                _passwordChangeResult.emit(PasswordChangeEvent.Failure("New password must be different from the old password."))
                return@launch
            }

            val user = withContext(Dispatchers.IO) {
                userDao.getUserByUsername(username)
            }

            if (user == null) {
                _passwordChangeResult.emit(PasswordChangeEvent.Failure("Error: Could not find user."))
            } else if (PasswordHasher.verify(oldPass, user.passwordHash)) {
                // Old password is correct, proceed to update
                val newHash = PasswordHasher.hash(newPass)
                val updatedUser = user.copy(passwordHash = newHash)

                withContext(Dispatchers.IO) {
                    userDao.updateUser(updatedUser)
                }
                _passwordChangeResult.emit(PasswordChangeEvent.Success("Password updated successfully!"))
            } else {
                // Old password was incorrect
                _passwordChangeResult.emit(PasswordChangeEvent.Failure("Incorrect old password."))
            }
        }
    }
}