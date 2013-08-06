# Sink
# Version: 2013-01-16
# Contact: Matthias.Buechse@tu-dresden.de
# Category: basics
# IN in :: t0
#
# Sink tool.
SinkTool () {
	echo "Sink: $2"
}

# runs a tool
# run2 <id> <n> <tool> <root> <i1> ... <in> <o1> ... <om>
# <id>    job id
# <n>     number of input arguments
# <m>     number of output arguments
# <tool>  tool to run
# <root>  directory to root temp and meta files
# <i?>    input argument
# <o?>    output argument
run () {
	args=("$@")
	echo "Running: ${args[0]}"
	mkdir -p "${args[3]}"
	
	logFile="${args[3]}/log"
	touch "$logFile"
	
	echo "Checking: ${args[0]}" >> "$logFile"
	inNew="${args[4]}"
	for (( i=5; i < $(( ${args[1]} + 3 )); i++ )); do
		if [[ "${args[i]}" -nt "$inNew" ]]; then
			inNew="${args[$i]}"
		fi
	done

	outOld="${args[$((${args[1]} + 3))]}"
	for (( i=$(( ${args[1]} + 3 )); i < $#; i++ )); do
		if [[ "${args[$i]}" -ot "$inNew" ]]; then
			outOld="${args[$i]}"
		fi
	done

	echo "$(date)" >> "$logFile"
	if [[ -f "$outOld" ]]; then
		if [[ "$inNew" -nt "$outOld" ]]; then
			echo "Running: ${args[0]}" >> "$logFile"
			"${@:3}" &>> "$logFile"
			echo "Returned: $?" >> "$logFile"
		else
			echo "Skipping: ${args[0]}" >> "$logFile"
			echo "  Reason: Up-to-date file(s) found." >> "$logFile"
		fi
	else
		echo "Running: ${args[0]}" >> "$logFile"
		"${@:3}" &>> "$logFile"
		echo "Returned: $?" >> "$logFile"
	fi

	echo "Done: ${args[0]}"
}
