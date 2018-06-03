package yal2jvm.common;

import java.util.Iterator;
import java.util.List;

import yal2jvm.ir.Statement;

public class StatementsIterator implements Iterator<Statement> {

    private Iterator<Statement> iterator;

    public StatementsIterator(List<Statement> statements) {
        this.iterator = statements.iterator();
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Statement next() {
        Statement s = iterator.next();
        if (s.isCleared())
            return next();
        return s;
    }

}
