package com.example.telegramdiplom.ui.screeens.register

import androidx.fragment.app.Fragment
import com.example.telegramdiplom.R
import com.example.telegramdiplom.database.AUTH
import com.example.telegramdiplom.utilits.*
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_enter_phone_number.*
import java.util.concurrent.TimeUnit


class EnterPhoneNumberFragment : Fragment(R.layout.fragment_enter_phone_number) {
    private lateinit var mPhoneNumber:String
    private lateinit var  mCallback:PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onStart() {
        super.onStart()
            //CaLLback который возвращает результат верификации
        mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) { //проверка верификации
                //Если верификация произведена, пользователь авторизируется без подтверждений по смс
                AUTH.signInWithCredential(credential).addOnCompleteListener{ task ->
                    if (task.isSuccessful){
                        showToast("Авторизация прошла успешно. Добро пожаловать")
                        restartActivity()
                    }else showToast(task.exception?.message.toString())
                }
            }

            override fun onVerificationFailed(p0: FirebaseException) { //если проблема с верификацией
              showToast(p0.message.toString())
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) { //получение id и token после отправки Code
                //функция сработает если верификация проходит первый раз, и отправлено смс с кодом
                replaceFragment(
                    EnterCodeFragment(
                        mPhoneNumber,
                        id
                    )
                )
            }
        }
        register_btn_next.setOnClickListener { sendCode() }
    }

    private fun sendCode() {
        //Проверка поля для ввода номера телефона, если поле пустое -> вывод сообщения, если нет, начало процедуры авторизации
       if(register_input_phone_number.text.toString().isEmpty()){
           showToast(getString(R.string.register_toast_enter_phone))
       } else{
          authUser()
       }
    }

    private fun authUser() {
        // Инициализация пользователя
        mPhoneNumber = register_input_phone_number.text.toString()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            mPhoneNumber,
            60,
            TimeUnit.SECONDS,
            APP_ACTIVITY,
            mCallback

        )
    }
}