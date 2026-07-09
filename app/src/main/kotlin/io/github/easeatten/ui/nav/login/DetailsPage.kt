package io.github.easeatten.ui.nav.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.ui.icons.iconArrowForward
import io.github.easeatten.ui.nav.NavDestination
import io.github.easeatten.ui.viewmodels.nav.LoginState
import io.github.easeatten.ui.viewmodels.nav.LoginViewModel
import io.github.easeatten.ui.viewmodels.nav.LoginViewModelFactory

@Composable
fun DetailsPage(navController: NavController) {
  val context = LocalContext.current.applicationContext

  // Data Repositories
  val userRepository = remember { UserRepository(context) }
  // `ViewModel` Synthesis
  val vmFactory = remember { LoginViewModelFactory(userRepository) }
  val vm: LoginViewModel = viewModel(factory = vmFactory)

  val state by vm.state.collectAsStateWithLifecycle()
  val login by vm.login.collectAsStateWithLifecycle()

  Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        bottomBar = { BottomBar(navController, vm, state) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) {
      Box(modifier = Modifier.fillMaxSize().padding(it)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
          Text(
              text = "Let's get you started",
              style = MaterialTheme.typography.displayLarge,
          )

          // Departmental Code
          OutlinedTextField(
              modifier = Modifier.fillMaxWidth().padding(5.dp),
              label = { Text("Departmental Code") },
              value = state.department,
              singleLine = true,
              onValueChange = { value -> vm.updateDepartment(value) },
              isError = state.isDepartmentInvalid == true,
          )

          // Roll Number
          OutlinedTextField(
              modifier = Modifier.fillMaxWidth().padding(5.dp),
              label = { Text("Roll Number") },
              value = state.roll,
              singleLine = true,
              onValueChange = { value -> vm.updateRoll(value) },
              isError = state.isRollInvalid == true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
          )

          Row(modifier = Modifier.fillMaxWidth()) {
            // Batch Year
            OutlinedTextField(
                modifier = Modifier.weight(1f).padding(5.dp),
                label = { Text("Batch Year") },
                value = state.year,
                singleLine = true,
                onValueChange = { value -> vm.updateYear(value) },
                isError = state.isYearInvalid == true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )

            // Semester
            OutlinedTextField(
                modifier = Modifier.weight(1f).padding(5.dp),
                label = { Text("Semester") },
                value = state.semester,
                singleLine = true,
                onValueChange = { value -> vm.updateSemester(value) },
                isError = state.isSemesterInvalid == true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
          }
        }
      }
    }

    when {
      state.isLoginInProgress -> {
        Dialog(onDismissRequest = {}) { CircularProgressIndicator(modifier = Modifier.size(35.dp)) }
      }

      !state.isLoginInProgress && state.loginFailed == false -> {
        vm.clearLoginState()
        navController.navigate(NavDestination.HOME.route()) { popUpTo(0) }
      }

      !state.isLoginInProgress && state.loginFailed == true -> {
        LaunchedEffect(state.loginErrorMessage) {
          if (state.loginErrorMessage != null) {
            snackbarHostState.showSnackbar(state.loginErrorMessage!!)
            vm.clearLoginState()
          }
        }
      }
    }
  }
}

@Composable
internal fun BottomBar(
    navController: NavController,
    vm: LoginViewModel,
    state: LoginState,
) {
  BottomAppBar(containerColor = MaterialTheme.colorScheme.surface) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
      Button(
          modifier = Modifier.width(60.dp).height(60.dp),
          contentPadding = PaddingValues(0.dp),
          enabled =
              state.isDepartmentInvalid == false &&
                  state.isRollInvalid == false &&
                  state.isYearInvalid == false &&
                  state.isSemesterInvalid == false,
          onClick = { vm.attemptLogin() },
      ) {
        Icon(
            imageVector = iconArrowForward,
            contentDescription = "Login",
        )
      }
    }
  }
}
