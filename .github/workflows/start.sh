cd "$(dirname "$1")"

echo "Saving the EULA"
echo "eula=true" > "eula.txt"

mkdir "plugins"
mv "$2" "plugins/"

echo "Running the tests"
echo "stop" | java -DIReallyKnowWhatIAmDoingISwear -Xmx1024M -jar "$1" nogui