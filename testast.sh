echo "test ast =================="
files=`ls test | grep TreeVisitor.java`
for filename in $files
do
    #filename="./test/LinearSearch.java" 
    output=`java -cp bin Tiger "./test/$filename" -dump ast`
    astout=`echo "$output" | sed '/^$/d'`
    echo "======================================"
    stdans=`cat "./test/$filename" | sed -e '/^\s*$/d' -e '/^\s*\/\//d'\
    -e 's/\t/  /g' -e 's/^\( *\).*{$/&\n\1{/' | sed -e '/[a-zA-Z0-9)] {$/s/ {$//'\
    | sed -e 's/^\( *\).*} else$/&\n\1else/' | sed -e '/} else$/s/ else$//' \
    | sed -e 's/^\( *\).*else if\(.*\)$/&\n\1  if\2/' | sed -e '/^ *else if.*$/s/ if.*$//'`
    echo "$astout" > "./txt/${filename}ast.txt"
    echo "$stdans" > "./txt/${filename}std.txt"
    echo "./test/$filename"
    diff <(echo "$astout") <(echo "$stdans")
done