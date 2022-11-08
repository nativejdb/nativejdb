package gdb.mi.service.command.output;

/**
 * The parsed output of the data-evaluate-expression command.
 *
 * --data-evaluate-expression (('java.lang.Object'*)(0x7ffff7a28438))->hub->name->value->data
 * ^done,value="0x7ffff7672818 \"java.io.BufferedInputStream\""
 *
 */

public class MIDataEvaluateExpressionInfo extends MIInfo {

    // The parsed information
    private String value;

    public MIDataEvaluateExpressionInfo(MIOutput record) {
        super(record);
        parse();
    }

    public String getValue() {
        return value;
    }

    /**
     * For value related to Java Strings, the value has the format:
     *  0xaddress \"stringValue\"
     * @return
     */
    public String getString() {
        var type = getValue().substring(getValue().lastIndexOf(' ') + 1);
        if (type.startsWith("\"")) {
            type = type.substring(1);
        }
        if (type.endsWith("\"")) {
            type = type.substring(0, type.length() - 1);
        }
        return type;
    }

    /**
     *  Find the relevant tag in the output record ("asm_insns") and then
     *  parse its value.
     */
    private void parse() {
        if (isDone()) {
            MIOutput out = getMIOutput();
            MIResultRecord rr = out.getMIResultRecord();
            if (rr != null) {
                MIResult[] results =  rr.getMIResults();
                for (int i = 0; i < results.length; i++) {
                    String var = results[i].getVariable();
                    if (var.equals("value")) { //$NON-NLS-1$
                        MIValue value = results[i].getMIValue();
                        if (value instanceof MIConst) {
                            this.value = ((MIConst) value).getCString();
                        }
                    }
                }
            }
        }
    }
}