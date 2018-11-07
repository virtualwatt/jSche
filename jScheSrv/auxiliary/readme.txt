jScheSrv utility installation steps:

1. Install the service using the "install_service.bat".
2. Run the service in Windows' "Services" (update it to use the necessary startup mode if necessary). Service startup logs can be found in the "logs\console.log".
3. Setup your schedules to execute events in the jScheConfigs folder

=========================

Advanced users service configuration information:

Use "update_config.bat" to edit or add any parameters. Run it once to update service settings, (re)start the service for new settings to take effect.
"config_manager.bat" allows to edit service parameters in the UI. Note that next time you will use the "update_config.bat" it will reset the parameters again.
"run_in_console.bat" allows to start the same service in console mode (e.g. if you need to start the service application under your user).
 Note that the same settings are used as they are set for the service, pay attention that it can conflict with the same service if it is started in the same time.
