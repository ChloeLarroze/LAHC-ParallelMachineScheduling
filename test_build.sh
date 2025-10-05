#exec
javac -d bin $(find . -name "*.java")

#run
java -cp bin test.SwapTest