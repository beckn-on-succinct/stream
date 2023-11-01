
if [ "$debug" = "y" -o "$debug" = "Y" ]; then
    export DEBUG_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=6001"
fi

java $DEBUG_OPTS -cp "target/classes:target/dependency/*" in.humbhionline.certbot.TestRunner $@
