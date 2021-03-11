rm ./out/myout/*.txt
echo "test ast =================="

files=`ls test | grep .java$`
for filename in $files
do
    echo "$filename ==============================="
    output=`java -cp bin Tiger "./test/$filename"`
    afteroptline=`grep -n "after optimization" <(echo "$output") | cut -f1 -d:`
    afteroptline=`expr $afteroptline - 1`
    beforeoptline=`grep -n "before optimization" <(echo "$output") | cut -f1 -d:`
    beforeoptline=`expr $beforeoptline + 1`
    beforeoutput=`sed -n ""$beforeoptline,$afteroptline"p" <(echo "$output")`
    afteroptline=`expr $afteroptline + 2`
    afteroutput=`sed -n "$afteroptline,$ p" <(echo "$output")`

    if [ "$beforeoutput" = "$afteroutput" ];then
        echo "output right!"
    else 
        echo "output false!"
        echo "$beforeoutput" >"./out/myout/${filename}_before.txt"
        echo "$afteroutput" >"./out/myout/${filename}_after.txt"
        diff "./out/myout/${filename}_before.txt" "./out/myout/${filename}_after.txt"
    fi
    echo "$filename ==============================="
done
rm ./test/*.c