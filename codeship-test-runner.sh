#!/bin/bash
#
# This test intends to run each test individually to isolate side effects of tests not shutting down an env properly
# 

TMP_DIR="/tmp"
START_SPLUNK_LOG="${TMP_DIR}/splunk_start.log";

echo "starting test runner";
FAILED_TEST="" ; 
FAIL_CODE="" ;
SHELL_PID=$$; # script process ID
echo "running in parent process $SHELL_PID"

# trap to execute on receiving TERM signal 
trap "exit $FAIL_CODE" TERM;

DOCKER="";


### Helper functions ###
#
#
die() {
	echo "dying $1"
	kill -s TERM $SHELL_PID
}

# parse args
while getopts ":d" opt; do
  case $opt in
    d) DOCKER="SET"
    ;;
    \?) echo "Invalid option -$OPTARG" >&2 ; die 1;
    ;;
  esac
done

detect_time_cmd() {
	TIME=""
	/usr/bin/time -v echo "ok" > /dev/null 2>&1 && TIME="/usr/bin/time -v"
	/usr/bin/time -l echo "ok" > /dev/null 2>&1 && TIME="/usr/bin/time -l" 
}

# Run a test or die (kill the shell process with the failure exit code)
run_test_or_die() {
	echo -n "###`date`###starting $1 ... "; 

	# try running each test file:
	( 
		${TIME} mvn test -Dtest=$1 > ${TMP_DIR}/$1.log 2>&1 && 
		echo "succeeded" &&
		grep '^Tests run:' ${TMP_DIR}/$1.log
	) || ( # in case a test command above failed:
		FAIL_CODE=$? && 
		echo "failed with exit code: $FAIL_CODE" ;
		FAILED_TEST="$1"; 
		cat ${TMP_DIR}/$1.log ;
		die $FAIL_CODE;
	);
}

start_splunk() {
	echo "starting splunk...";
	(
		/opt/splunk/bin/splunk start > $START_SPLUNK_LOG 2>&1 && 
		echo 'last 100 records from $START_SPLUNK_LOG' && 
		tail -100 $START_SPLUNK_LOG 
	) || (
		FAIL_CODE=$? && 
		tail -10000 $START_SPLUNK_LOG;
		die $FAIL_CODE
	);
}


### Different flavors of tests ###
#
#

run_unit_tests_one_by_one() {
	UTESTS="`find src/test/java/ -iname \*Test\.java | grep -v -i -E 'Abstract|performance_tests|ConnectionSettingsTest|PropertiesConfigurationTest'|sed 's#.*/##;s#.java##'`";
	echo "Run all unit tests one by one"
	for f in $UTESTS; do 
		run_test_or_die $f; 
	done ; 
}

run_integration_tests_one_by_one() {
	ITESTS="`find src/test/java/ -iname \*IT\.java | grep -v -i -E 'Abstract|performance_tests|ConnectionSettingsTest'|sed 's#.*/##;s#.java##'`";
	echo "Run all integration tests one by one"
	for f in $ITESTS; do 
		run_test_or_die $f; 
	done
}

run_unit_tests_one_shot() {
	( 
		mvn clean test > ${TMP_DIR}/run_unit_tests_one_shot.log 2>&1 && 
		grep '^Tests run:' ${TMP_DIR}/run_unit_tests_one_shot.log 
	) || (
		FAIL_CODE=$?;
		cat ${TMP_DIR}/run_unit_tests_one_shot.log;
		die $FAIL_CODE;
	)
}

run_all_tests_one_shot() {
	( 
		mvn clean verify -DskipITs=false > ${TMP_DIR}/run_all_tests_one_shot.log 2>&1 && 
		grep '^Tests run:' ${TMP_DIR}/run_all_tests_one_shot.log 
	) || (
		FAIL_CODE=$?;
		cat ${TMP_DIR}/run_all_tests_one_shot.log;
		die $FAIL_CODE;
	)
}


### Main ###
#
#
echo "Detecting time cmd..."
detect_time_cmd
echo "TIME: ${TIME}"

#run_unit_tests_one_by_one

if [ "$DOCKER" ]; then 
	start_splunk
else 
	echo "running locally, expect splunk to be run on the local host for integration tests"
fi

#run_integration_tests_one_by_one
run_all_tests_one_shot


