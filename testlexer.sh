echo ==============
echo test starting
echo =============
check_java_version=`java -version 2>&1` #java version output to stderr,same with python
echo "check java version is: $check_java_version"

files=`ls test | grep .java`
#echo $files
check_lexer=false
for file in $files
do
    echo "./test/$file"
    if [ "$check_lexer" = true ]
    then
        output=`java -cp bin Tiger "./test/$file" -testlexer`
        noneout=`echo "$output" | grep TOKEN_MAIN`    
        echo "$noneout"
    else
        output=`java -cp bin Tiger "./test/$file"`
        echo "$output"
    fi
    echo "------------------------------"
done
echo ===========
echo test finished
echo ================
