package operations;

import ir.Statement;

@FunctionalInterface
public interface AddOperation {

    AddOperation add(Operation operation);

    default AddOperation addNode(LowIrNode node, Statement statement) {
        return operation -> {
            LowIrNode newNode = new LowIrNode(operation);
            operation.setContainer(newNode);
            operation.setStatement(statement);
            node.addChild(newNode);
            return addNode(newNode, statement);
        };
    }

}
