#!/usr/bin/env bash
# -----------------------------------------------------------------------------
# Startet den für die Tests notwendigen Container
#
# Dieses Script wartet bis der Server auf port 8080 tatsächlich verfügbar ist
# -----------------------------------------------------------------------------

# DEV_LOCAL muss in .bashrc gesetzt werden. ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
if [ -z ${DEV_DOCKER+x} ]; then echo "Var 'DEV_DOCKER' nicht gesetzt!"; exit 1; fi
# ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

# Abbruch bei Problemen (https://goo.gl/hEEJCj)
#
# Wenn ein Fehler nicht automatisch zu einem exit führen soll dann
# kann 'command || true' verwendet werden
#
# Für die $1, $2 Abfragen kann 'CMDLINE=${1:-}' verwendet werden
#
# -e Any subsequent(*) commands which fail will cause the shell script to exit immediately
# -o pipefail sets the exit code of a pipeline to that of the rightmost command
# -u treat unset variables as an error and exit
# -x print each command before executing it
set -eou pipefail

APPNAME="`basename $0`"

SCRIPT=`realpath $0`
SCRIPTPATH=`dirname ${SCRIPT}`

#------------------------------------------------------------------------------
# Set WORKSPACE
#
cd ${SCRIPTPATH}

# Zum testen werden die Container verwendet die auf Amazon sind
# Diese Container sind die "released"en Container
AMAZON_REPO_URI="936985261795.dkr.ecr.eu-west-1.amazonaws.com"

CONTAINERS=(
    "webappbase"
    "webappbase-db-test"
)

# IntelliJ-Module (Project-path)
MODULE_PATH=`realpath ../..`

#------------------------------------------------------------------------------
# Libs
#

# LIB_DIR="${DEV_DOCKER}/_global/lib"
TOOLS_LIB="tools.lib.sh"

if [[ ! -f "${DEV_BASH}/${TOOLS_LIB}" ]]
then
    echo "LibDir ${DEV_BASH}/${TOOLS_LIB} existiert nicht!"
    exit 1
fi

. "${DEV_BASH}/${TOOLS_LIB}"

#------------------------------------------------------------------------------
# Functions
#

pullContainer() {
    REGION=${1:-'eu-west-1'}

    type aws >/dev/null 2>&1 || { echo >&2 "$(colorError 'aws is required but it''s not installed!')"; exit 1; }

    # Anfrage für das Docker-Login...
    $(aws ecr get-login --no-include-email --region ${REGION})

    # Loop Through ARRAY
    for CONTAINER in ${CONTAINERS[@]}
    do
        docker pull "${AMAZON_REPO_URI}/${CONTAINER}"
    done
}

startContainer() {
    # cp -a "${MOBIAD_LIBRARY_DOCKER}/.env" "${MODULE_PATH}/docker/_ci"
    #cp -a "${MOBIAD_LIBRARY_DOCKER}/docker-compose.yml" "${MODULE_PATH}/docker/_ci"

    docker-compose -f "docker-compose.yml" up -d

    # echo
    # echo $(colorGreen "Waiting for port 8080 to become ready...")

    # MAX_CHECKS=10
    # counter=0
    # until docker-compose logs  | grep -i -C 5 "Embedded Server runs on.*8080"; do
    #     >&2 echo "   └ waiting... (${counter} < ${MAX_CHECKS})"
    #
    #     counter=$((counter+1))
    #     if test "${counter}" -gt ${MAX_CHECKS}; then
    #         docker-compose logs
    #         exit 1
    #     fi
    #     sleep 2
    # done
}

stopContainer() {
    docker-compose -f "docker-compose.yml" down --volume
}

showLogs() {
    docker-compose -f "docker-compose.yml" logs -f
}

#------------------------------------------------------------------------------
# Options
#

usage() {
    echo
    echo "Usage: ${APPNAME} [ options ]"
    echo -e "\t--pull     Pull necessary containers"
    echo -e "\t--start    Start container"
    echo -e "\t--stop     Stop container"
    echo -e "\t--logs     Show logs"
    echo
}


CMDLINE=${1:-}
case "$CMDLINE" in
    pull|-pull|--pull)
        pullContainer
    ;;

    start|-start|--start)
        startContainer
    ;;

    stop|-stop|--stop)
        stopContainer
    ;;

    logs|-logs|--logs)
        showLogs
    ;;

    -h|-help|--help|*)
        usage
    ;;

esac

#------------------------------------------------------------------------------
# Alles OK...

exit 0
