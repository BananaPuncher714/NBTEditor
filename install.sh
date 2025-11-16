export REV=1.8.8
export M2_DIRECTORY="$M2_REPOSITORY"

REPOSITORY_DIR=$(dirname "$0")

cd "$REPOSITORY_DIR"

git submodule update --init --recursive --remote

./.git-submodules/spigot-docker-build-tools/run.sh mvn clean install
