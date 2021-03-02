echo ==============
echo test starting
echo =============
check_java_version=`java -version 2>&1` #java version output to stderr,same with python
echo "check java version is: $check_java_version"

files=`ls test | grep .java`
#echo $files

for file in $files
do
    echo "./test/$file"
    output=`java -cp bin Tiger "./test/$file" -testlexer`
    #echo "$output"
    noneout=`echo "$output" | grep TOKEN_MAIN`    
    echo "$noneout"
    echo "------------------------------"
done
echo ===========
echo test finished
echo ================
