#------------------------------------------------------------------------------
#
# Basis:
#   https://hub.docker.com/r/mdillon/postgis/
#   https://hub.docker.com/_/postgres/
#
#------------------------------------------------------------------------------

FROM mikemitterer/postgres

MAINTAINER Mike Mitterer <office@mikemitterer.at>

# - Setup Postgres ------------------------------------------------------------
ENV POSTGRES_DB catshostel

COPY config/docker-entrypoint-initdb.d/* /docker-entrypoint-initdb.d/

# - INIT ----------------------------------------------------------------------

ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["postgres"]
