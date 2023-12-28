package com.virtualstudios.extensionfunctions.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.virtualstudios.extensionfunctions.Constants
import javax.inject.Inject

class AppUserPreferences @Inject constructor(
    private val sharedPreferences: SharedPreferences
){

    fun setFullName(fullName: String){
        sharedPreferences.edit {
            putString(Constants.KEY_FULL_NAME, fullName)
        }
    }

    fun getFullName() = sharedPreferences.getString(Constants.KEY_FULL_NAME, null)

    fun setEmail(emailAddress: String){
        sharedPreferences.edit {
            putString(Constants.KEY_EMAIL, emailAddress)
        }
    }

    fun getEmail() = sharedPreferences.getString(Constants.KEY_EMAIL, null)

    fun setDialCode(dialCode: String){
        sharedPreferences.edit {
            putString(Constants.KEY_DIAL_CODE, dialCode)
        }
    }

    fun getDialCode() = sharedPreferences.getString(Constants.KEY_DIAL_CODE, null)

    fun setDialCodeIso(dialCodeIso: String){
        sharedPreferences.edit {
            putString(Constants.KEY_DIAL_CODE_ISO, dialCodeIso)
        }
    }

    fun getDialCodeIso() = sharedPreferences.getString(Constants.KEY_DIAL_CODE_ISO, null)

    fun setMobileNumber(mobileNumber: String){
        sharedPreferences.edit {
            putString(Constants.KEY_MOBILE_NUMBER, mobileNumber)
        }
    }

    fun getMobileNumber() = sharedPreferences.getString(Constants.KEY_MOBILE_NUMBER, null)

    fun setProfileImageUrl(profileImageUrl: String){
        sharedPreferences.edit {
            putString(Constants.KEY_PROFILE_IMAGE_URL, profileImageUrl)
        }
    }

    fun getProfileImageUrl() = sharedPreferences.getString(Constants.KEY_PROFILE_IMAGE_URL, null)

    fun setAccessToken(accessToken: String){
        sharedPreferences.edit {
            putString(Constants.KEY_ACCESS_TOKEN, accessToken)
        }
    }

    fun getAccessToken() = sharedPreferences.getString(Constants.KEY_ACCESS_TOKEN, null)

    fun setFcmToken(fcmToken: String){
        sharedPreferences.edit {
            putString(Constants.KEY_FCM_TOKEN, fcmToken)
        }
    }

    fun getFcmToken() = sharedPreferences.getString(Constants.KEY_FCM_TOKEN, null)

    fun setIsLoggedIn(loggedIn: Boolean){
        sharedPreferences.edit {
            putBoolean(Constants.KEY_IS_LOGGED_IN, loggedIn)
        }
    }

    fun getIsLoggedIn() = sharedPreferences.getBoolean(Constants.KEY_IS_LOGGED_IN, false)

    fun clearUserPreferences(){
        sharedPreferences.edit {
            clear()
        }
    }


}