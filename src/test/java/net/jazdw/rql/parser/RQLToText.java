/**
 * Copyright (C) 2015 Jared Wiltshire. All rights reserved.
 */
package net.jazdw.rql.parser;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Jared Wiltshire
 */
public class RQLToText implements SimpleASTVisitor<String> {
    public static void main(String args[]) {
        RQLParser parser = new RQLParser();
        
        String input, output;
        SimpleASTVisitor<String> visitor = new RQLToText();
        
        input = "(name=jack|name=jill)&age>30";
        output = parser.parse(input, visitor);
        System.out.println(String.format("'%s' => %s", input, output));
        
        input = "(name=jack|name=jill)&date<2015-05-10T14:18:33&sort(-date)";
        output = parser.parse(input, visitor);
        System.out.println(String.format("'%s' => %s", input, output));
        
        input = "(name=jack|name=jill)&date<isodate:2015-05-10T14:18:33&sort(name)";
        output = parser.parse(input, visitor);
        System.out.println(String.format("'%s' => %s", input, output));
        
        input = "name=jack|name=jill&date<epoch:1431267513000&sort(name,-date)";
        output = parser.parse(input, visitor);
        System.out.println(String.format("'%s' => %s", input, output));
        
        input = "name=jill&age=number:8&limit(10,30)";
        output = parser.parse(input, visitor);
        System.out.println(String.format("'%s' => %s", input, output));
    }
    
    /* (non-Javadoc)
     * @see net.jazdw.rql.parser.SimpleASTVisitor#visit(net.jazdw.rql.parser.ASTNode)
     */
    @Override
    public String visit(ASTNode node) {
        switch (node.getName()) {
        case "and":
        case "or":
            return visitAndOr(node);
        case "eq":
            return node.format("%s=%s");
        case "gt":
            return node.format("%s>%s");
        case "ge":
            return node.format("%s>=%s");
        case "lt":
            return node.format("%s<%s");
        case "le":
            return node.format("%s<=%s");
        case "ne":
            return node.format("%s!=%s");
        case "sort":
            return visitSort(node);
        case "limit":
            return visitLimit(node);
        default:
            return node.toString();
        }
    }
    
    private String visitLimit(ASTNode node) {
        if (node.getArgumentsSize() == 2) {
            return node.format("limit %s offset %s");
        } else {
            return node.format("limit %s");
        }
    }

    public String visitSort(ASTNode node) {
        List<String> sortProps = new ArrayList<String>();
        for (Object obj : node) {
            boolean descending = false;
            String prop = (String) obj;
            if (prop.startsWith("-")) {
                descending = true;
                prop = prop.substring(1);
            } else if (prop.startsWith("+")) {
                prop = prop.substring(1);
            }
            String ascDesc = descending ? " desc" : " asc";
            sortProps.add(prop + ascDesc);
        }
        return "sort by " + StringUtils.join(sortProps, ", ");
    }
    
    public String visitAndOr(ASTNode node) {
        List<String> components = new ArrayList<String>();
        for (Object obj : node) {
            if (obj instanceof ASTNode) {
                components.add(((ASTNode) obj).accept(this));
            } else {
                throw new RuntimeException("AND/OR terms should only have ASTNode arguments");
            }
        }
        return "(" + StringUtils.join(components, ") " + node.getName().toUpperCase() + " (") + ")";
    }
}