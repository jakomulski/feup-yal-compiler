package yal2jvm.ir.operations;

import yal2jvm.ir.Statement;

@FunctionalInterface
public interface AddOperation {

    AddOperation add(Operation operation);

    default AddOperation addNode(LowIrNode node, Statement statement) {
        return operation -> {
            LowIrNode newNode = new LowIrNode(operation, statement);
            operation.setContainer(newNode);
            operation.setStatement(statement);
            node.addChild(newNode);
            return addNode(newNode, statement);
        };
    }

}
