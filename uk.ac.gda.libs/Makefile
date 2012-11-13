# Allows each JAR to be unpacked into its own directory
# Useful for analysing the JARs

unpack:
	@for jar in *.jar; do \
		dir=`basename $$jar .jar`; \
		mkdir $$dir; \
		unzip -oqd $$dir $$jar; \
	done

clean:
	@for jar in *.jar; do \
		dir=`basename $$jar .jar`; \
		rm -rf $$dir; \
	done
