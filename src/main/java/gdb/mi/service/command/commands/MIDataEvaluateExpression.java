package gdb.mi.service.command.commands;

import gdb.mi.service.command.output.MIDataEvaluateExpressionInfo;
import gdb.mi.service.command.output.MIOutput;

/**
 * -data-evaluate-expression expr
 */

public class MIDataEvaluateExpression extends MICommand<MIDataEvaluateExpressionInfo> {

    public MIDataEvaluateExpression(String expression) {
        super("-data-evaluate-expression");
        setOptions(new String[]{expression});
    }

    @Override
    public MIDataEvaluateExpressionInfo getResult(MIOutput output) {
        return new MIDataEvaluateExpressionInfo(output);
    }
}