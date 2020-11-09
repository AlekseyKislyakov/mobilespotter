# mobilespotter
App to detect current user of mobile device that is rented.
Technologies:
Kotlin coroutines, LiveData, ViewModel, Hilt, Jetpack Navigation, etc.

#Structure
##View
View layer is implemented in single activity style (MainActivity) that has a fragment view container
The fragments that are inserted into container implement BaseFragment interface, which contains:
1) onSetupLayout() - for layout operations
2) onBindViewModel() - for LiveData and other bindings with VM
3) observeOperations() - for observing live events from VM - initialized once the fragment is created (otherwise caused bug with multiple observe of one event)
4) onCodeRecognized(code: String) - for futher operations with recognized rfid code. Each fragment has its own implementation of this method
5) showFloatingActionButton: Boolean - for show/hide fab with rfid dialog

##ViewModel
Is implemented in BaseViewModel interface that has no methods. Logic is here

##Domain
Domain layer is implemented in UseCases each of them contain only one remote method.
There are two types of UseCases: simple UseCase (that receives no params from user) and UseCaseWithRequestedParams (waits for params from user)

##Remote
Remote layer contains two sources of information: Preferences and Retrofit service
Preferences is implemented in single file PreferencesStorage.kt and has simple SharedPreferences implementation
Retrofit service contains factory (ApiServiceFactory.kt) implementation and ApiService.kt interface that contains all the methods

____

##TODO
1) Implement multiple operations in one observe method
