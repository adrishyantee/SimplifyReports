package project.absurdnerds.simplify.login

import android.annotation.SuppressLint
import android.graphics.Color
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import cn.pedant.SweetAlert.SweetAlertDialog
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.login_mobile_fragment.*
import project.absurdnerds.simplify.FragmentChangeInterface
import project.absurdnerds.simplify.R
import project.absurdnerds.simplify.utils.showToast
import timber.log.Timber
import java.util.concurrent.TimeUnit

class LoginMobileFragment : Fragment() {

    companion object {
        fun newInstance() = LoginMobileFragment()
    }

    private lateinit var viewModel: LoginMobileViewModel
    private lateinit var fragmentChangeInterface: FragmentChangeInterface
    private var mobileNumber: String = ""
    private var storedVerificationId: String = ""
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.login_mobile_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginMobileViewModel::class.java)

        init()

        buttonGetOtp.setOnClickListener {

            if (etLoginMobile.text.isNullOrEmpty()) {
                showToast(getString(R.string.mobile_number_cant_be_blank))
                return@setOnClickListener
            }

            mobileNumber = "+${etCountryCode.selectedCountryCode.toString()+etLoginMobile.text.toString()}"
            firebaseAuth()

        }


    }

    private fun init() {
        fragmentChangeInterface = context as FragmentChangeInterface
        firebaseAuth = FirebaseAuth.getInstance()
    }


    @SuppressLint("ResourceType")
    private fun firebaseAuth() {

        var sweetAlertDialog = SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE)
        sweetAlertDialog.progressHelper.barColor = Color.parseColor(resources.getString(R.color.progressBarColor))
        sweetAlertDialog.titleText = getString(R.string.loading)
        sweetAlertDialog.setCancelable(false)
        sweetAlertDialog.show()

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

                Timber.d(getString(R.string.phone_verified))

            }

            override fun onVerificationFailed(e: FirebaseException) {

                Timber.e(e.message.toString())
                sweetAlertDialog.cancel()

                SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText(getString(R.string.error_sending_otp))
                    .show()

            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {

                sweetAlertDialog.cancel()
                Timber.d("onCodeSent:$verificationId")
                storedVerificationId = verificationId


                var bundle = Bundle()
                bundle.putString("mobileNumber", mobileNumber)
                bundle.putString("storedVerificationId", storedVerificationId)

                var fragment = LoginOTPFragment()
                fragment.arguments = bundle
                fragmentChangeInterface.changeFragment(fragment)

            }

        }

        Timber.d(mobileNumber)

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            mobileNumber, // Phone number to verify
            120, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            activity!!, // Activity (for callback binding)
            callbacks
        ) // OnVerificationStateChangedCallbacks

    }

}