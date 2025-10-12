#delete the old bin folder
rm -rf bin

#exec
javac -d bin $(find . -name "*.java")

#run
java -cp bin test.SwapTest
java -cp bin test.LocalSearchTest

