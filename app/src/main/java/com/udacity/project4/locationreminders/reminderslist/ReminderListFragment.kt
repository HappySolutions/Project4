package com.udacity.project4.locationreminders.reminderslist

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.IntentCommand
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.ReminderDescriptionActivity
import com.udacity.project4.utils.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReminderListFragment : BaseFragment() {
    //use Koin to retrieve the ViewModel instance
    override val _viewModel: RemindersListViewModel by viewModel()
    private lateinit var binding: FragmentRemindersBinding
    val _authenticationViewModel: AuthenticationViewModel by inject()
    private lateinit var fragmentContext: Context
    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_reminders, container, false
            )
        binding.viewModel = _viewModel
        fragmentContext = binding.addReminderFAB.context

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(false)
        setTitle(fragmentContext.getString(R.string.app_name))

        setupObserver()

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }
        enableMyLocation()

        return binding.root
    }
    private fun setupObserver() {
        _authenticationViewModel.navigateBackToAuth.observe(
            viewLifecycleOwner
        ) { navToAuthActivity ->
            if (navToAuthActivity) {
                createIntentForNavToAuthActivity()
            }
        }

        _viewModel.intentCommand.observe(viewLifecycleOwner) { command ->
            if (command is IntentCommand.BackTo) {
                navigateToAuthActivity(command)
            } else
                if (command is IntentCommand.ToReminderDescriptionActivity) {
                    navToReminderDescActivity(command)
                }
        }
    }

    private fun navigateToAuthActivity(command: IntentCommand.BackTo) {
        val intent = Intent(context, command.activity)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun createIntentForNavToAuthActivity() {
        _viewModel.intentCommand.postValue(
            IntentCommand.BackTo(AuthenticationActivity::class.java)
        )
    }

    private fun navToReminderDescActivity(command: IntentCommand.ToReminderDescriptionActivity) {
        val intent = ReminderDescriptionActivity.newIntent(command.from, command.item)
        startActivity(intent)
    }

    private fun enableMyLocation() {
        if (isForegroundLocationGrantedFromContext(fragmentContext)
        ) {
            if (!areLocationServicesEnabled(fragmentContext)) {
                promptUserToEnableLocationServices()
            }
        } else {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
            )
        }
    }
    private fun promptUserToEnableLocationServices() {
        _viewModel.showErrorMessage.postValue(fragmentContext.getString(R.string.location_required_error))
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (isRequestCodeEqualLocationPermissionCode(requestCode)) {
            if (isForegroundLocationPermissionGrantedFromResult(grantResults)) {
                enableMyLocation()
            } else {
                if (areLocationServicesEnabled(fragmentContext)) {
                    promptUserToGrantLocationPermission()
                } else {
                    promptUserToEnableLocationServices()
                }
            }
        }
    }

    private fun promptUserToGrantLocationPermission() {
        _viewModel.showErrorMessage.postValue(fragmentContext.getString(R.string.permission_denied_explanation))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder() {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder()
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {item ->
            createIntentForNavToRemDescActivity(item)
        }
//        setup the recycler view using the extension function
        binding.reminderssRecyclerView.setup(adapter)
    }

    private fun createIntentForNavToRemDescActivity(item: ReminderDataItem) {
        _viewModel.intentCommand.postValue(
            IntentCommand.ToReminderDescriptionActivity(
                this.activity as AppCompatActivity,
                ReminderDescriptionActivity::class.java,
                item
            )
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                _authenticationViewModel.logout(requireContext())
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.main_menu, menu)
    }

}
