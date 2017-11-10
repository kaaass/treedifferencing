/*
 * This file is part of GumTree.
 *
 * GumTree is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * GumTree is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with GumTree. If
 * not, see <http://www.gnu.org/licenses/>.
 *
 *
 * Copyright 2015-2016 Georg Dotzler <georg.dotzler@fau.de>
 * Copyright 2015-2016 Marius Kamp <marius.kamp@fau.de>
*/
package com.github.gumtreediff.tree;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.tree.TreeUtils;

import de.fosd.jdime.artifact.ast.ASTNodeArtifact;

import org.extendj.ast.ASTNode;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * The Class ASTNodeArtifactWrapper.
 */
public class AstNodeArtifactWrapper implements ITree {
    private static final int COLUMN_FIELD_BITS = 12;
    private static final int COLUMN_FIELD_MASK = (1 << COLUMN_FIELD_BITS) - 1;

    public ASTNodeArtifact tree = null;
    private List<ASTNodeArtifact> nodeDirectChildrenForWrappedAccess =
            new LinkedList<ASTNodeArtifact>();
    private List<ITree> nodeDirectChildrenTree = new LinkedList<ITree>();
    private ITree parent;
    private int size;
    private int pos;
    private int length;
    private int id = -1;

    String label = null;
    private int hash;

    /**
     * Instantiates a new AST node artifact wrapper.
     *
     * @param tree the tree
     */
    public AstNodeArtifactWrapper(ASTNodeArtifact tree) {
        this.tree = tree;
        List<ASTNodeArtifact> tmp = tree.getChildren();
        for (ASTNodeArtifact child : tmp) {
            AstNodeArtifactWrapper ichild = new AstNodeArtifactWrapper((ASTNodeArtifact) child);
            addChild(ichild);
            nodeDirectChildrenForWrappedAccess.add(child);

        }

    }

    /**
     * Instantiates a new i node wrapper extern.
     *
     * @param wrapper the wrapper
     */
    public AstNodeArtifactWrapper(AstNodeArtifactWrapper wrapper) {
        this.tree = wrapper.tree;
        this.setPos(wrapper.pos);
        this.setLength(wrapper.length);
        this.label = wrapper.label;
        this.nodeDirectChildrenForWrappedAccess = wrapper.nodeDirectChildrenForWrappedAccess;
        for (ITree child : wrapper.getChildren()) {
            addChild(new AstNodeArtifactWrapper((AstNodeArtifactWrapper) child));
        }
    }

    /**
     * Compute positions.
     *
     * @param root the root
     * @param data the data
     */
    public static void computePositions(ITree root, byte[] data) {
        ByteArrayInputStream stream = new ByteArrayInputStream(data);
        Stream<String> lines = new BufferedReader(new InputStreamReader(stream)).lines();
        HashMap<Integer, Integer> lineOffsets = new HashMap<>();
        lineOffsets.put(1, 0);
        int start = 2;
        int offset = 0;
        LinkedList<String> strings = new LinkedList<>();
        lines.forEachOrdered(s -> strings.add(s));
        for (String s : strings) {
            offset += s.length() + 1;
            lineOffsets.put(start, offset);
            start++;
        }
        for (ITree tree : root.postOrder()) {
            AstNodeArtifactWrapper wrapper = (AstNodeArtifactWrapper) tree;
            ASTNodeArtifact artifact = wrapper.tree;
            ASTNode<?> node = artifact.getASTNode();
            int startPos = node.getStart();
            int end = node.getEnd();
            if (startPos != 0 && end != 0) {
                int startLine = (startPos ^ COLUMN_FIELD_MASK) >> COLUMN_FIELD_BITS;
                int startCol = (startPos & COLUMN_FIELD_MASK) - 1;
                int endLine = (end ^ COLUMN_FIELD_MASK) >> COLUMN_FIELD_BITS;
                int endCol = (end & COLUMN_FIELD_MASK);
                int computedStart = lineOffsets.get(startLine) + startCol;
                int computedEnd = lineOffsets.get(endLine) + endCol;
                tree.setPos(computedStart);
                tree.setLength(computedEnd - computedStart);
            } else {
                if (tree.getParent() != null) {
                    tree.setLength(tree.getParent().getLength());
                    tree.setPos(tree.getParent().getPos());
                }
            }

        }
    }

    @Override
    public void addChild(ITree tree) {
        nodeDirectChildrenTree.add((AstNodeArtifactWrapper) tree);
        tree.setParent(this);
    }

    @Override
    public ITree deepCopy() {
        AstNodeArtifactWrapper copy = new AstNodeArtifactWrapper(this);
        return copy;
    }

    @Override
    public int getChildPosition(ITree child) {
        assert (false);
        return 0;
    }

    @Override
    public List<ITree> getChildren() {
        return nodeDirectChildrenTree;
    }

    @Override
    public ITree getChild(int position) {
        return getChildren().get(position);
    }

    @Override
    public int getDepth() {
        assert (false);
        return 0;
    }

    @Override
    public List<ITree> getDescendants() {
        assert (false);
        return null;
    }

    @Override
    public int getHash() {
        return hash;
    }

    @Override
    public int getHeight() {
        assert (false);
        return 0;
    }

    @Override
    public int getId() {
        return tree.getNumber();
    }

    @Override
    public boolean hasLabel() {
        assert (false);
        return false;
    }

    @Override
    public String getLabel() {
        if (label == null) {
            String tmpFirst = this.tree.toString();
            // String[] split = tmpFirst.split("=");
            // String tmp = split.length > 1 ? split[1] : "";
            // tmp = tmp.replaceFirst(this.tree.getASTNode().getClass().getName(), "").trim();
            ExtendjTypes enumVariable = ExtendjTypes.ENUMERATION_MAP.get(getType());
            StringBuffer buffer = new StringBuffer();
            int pos = 0;
            while (pos < tmpFirst.length()) {
                while (pos < tmpFirst.length() && tmpFirst.charAt(pos) != '=') {
                    pos++;
                }
                pos++;
                pos++;
                while (pos + 1 < tmpFirst.length()
                        && !(tmpFirst.charAt(pos) == '"' && tmpFirst.charAt(pos + 1) == ' ')) {
                    if (tmpFirst.charAt(pos) == '"') {
                        buffer.append("\\");
                    }
                    buffer.append(tmpFirst.charAt(pos));
                    pos++;
                }
                pos++;
            }
            String tmp = buffer.toString();
            if (tmp.length() != 0 && (enumVariable == null || !enumVariable.hasValue)) {
                switch (enumVariable) {
                    case SUPER_ACCESS:
                    case THIS_ACCESS:
                    case SUPER_CONSTRUCTOR_ACCESS:
                    case CONSTRUCTOR_ACCESS:
                        tmp = "";
                        break;
                    default:
                        System.out.println("problem: " + this.tree.getASTNode().getClass().getName()
                                + " " + tmp);
                        assert (false) : this.tree.getASTNode().getClass().getName();
                        System.exit(-1);
                }
            }
            // tmp = tmp.replaceFirst("\\[", "").trim();
            // int pos = tmp.lastIndexOf("]");
            // if (pos != -1) {
            // tmp = tmp.substring(0, pos).trim();
            // }
            label = tmp;
            // System.out.println(enumVariable + " : " + label);
        }

        return label;
    }

    @Override
    public int getLength() {
        return this.length;
    }

    @Override
    public ITree getParent() {
        return parent;
    }

    @Override
    public List<ITree> getParents() {
        assert (false);
        return null;
    }

    @Override
    public int getPos() {
        return pos;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public List<ITree> getTrees() {
        return TreeUtils.preOrder(this);
    }

    /**
     * Gets the type.
     *
     * @return the type
     */
    @Override
    public int getType() {
        return getTypeWrapped();
    }

    public static enum ExtendjTypes {
        PROGRAM("org.extendj.ast.Program", 0, false), LIST("org.extendj.ast.List", 1, false),
        COMPILATION_UNIT("org.extendj.ast.CompilationUnit", 2, true),
        SINGLE_STATIC_IMPORT_DECL("org.extendj.ast.SingleStaticImportDecl", 3, true),
        TYPE_ACCESS("org.extendj.ast.TypeAccess", 4, true),
        SINGLE_TYPE_IMPORT_DECL("org.extendj.ast.SingleTypeImportDecl", 5, false),
        CLASS_DECL("org.extendj.ast.ClassDecl", 6, true),
        MODIFIERS("org.extendj.ast.Modifiers", 7, false),
        MODIFIER("org.extendj.ast.Modifier", 8, true), OPT("org.extendj.ast.Opt", 9, false),
        METHOD_DECL("org.extendj.ast.MethodDecl", 10, true),
        BLOCK("org.extendj.ast.Block", 11, false),
        RETURN_STMT("org.extendj.ast.ReturnStmt", 12, false),
        CLASS_INSTANCE_EXPR("org.extendj.ast.ClassInstanceExpr", 13, false),
        FIELD_DECLARATION("org.extendj.ast.FieldDeclaration", 14, true),
        STRING_LITERAL("org.extendj.ast.StringLiteral", 15, true),
        CONSTRUCTOR_DECL("org.extendj.ast.ConstructorDecl", 16, true),
        ANNOTATION("org.extendj.ast.Annotation", 17, true),
        THIS_ACCESS("org.extendj.ast.ThisAccess", 18, false),
        PARAMETER_DECLARATION("org.extendj.ast.ParameterDeclaration", 19, true),
        EXPR_STMT("org.extendj.ast.ExprStmt", 20, false),
        ASSIGN_SIMPLE_EXPR("org.extendj.ast.AssignSimpleExpr", 21, false),
        VAR_ACCESS("org.extendj.ast.VarAccess", 22, true),
        PRIMITIVE_TYPE_ACCESS("org.extendj.ast.PrimitiveTypeAccess", 23, true),
        PAR_TYPE_ACCESS("org.extendj.ast.ParTypeAccess", 24, false),
        WILDCARD("org.extendj.ast.Wildcard", 25, false), DOT("org.extendj.ast.Dot", 26, false),
        METHOD_ACCESS("org.extendj.ast.MethodAccess", 27, true),
        WILDCARD_EXTENDS("org.extendj.ast.WildcardExtends", 28, false),
        MEMBER_CLASS_DECL("org.extendj.ast.MemberClassDecl", 29, false),
        TRY_STMT("org.extendj.ast.TryStmt", 30, false),
        BASIC_CATCH("org.extendj.ast.BasicCatch", 31, false),
        IF_STMT("org.extendj.ast.IfStmt", 32, false),
        THROW_STMT("org.extendj.ast.ThrowStmt", 33, false),
        VAR_DECL_STMT("org.extendj.ast.VarDeclStmt", 34, false),
        VARIABLE_DECL("org.extendj.ast.VariableDecl", 35, false),
        PACKAGE_ACCESS("org.extendj.ast.PackageAccess", 36, true),
        PAR_METHOD_ACCESS("org.extendj.ast.ParMethodAccess", 37, true),
        ENHANCED_FOR_STMT("org.extendj.ast.EnhancedForStmt", 38, true),
        VARIABLE_DECLARATION("org.extendj.ast.VariableDeclaration", 39, true),
        NULL_LITERAL("org.extendj.ast.NullLiteral", 40, true),
        EQ_EXPR("org.extendj.ast.EQExpr", 41, false),
        AND_LOGICAL_EXPR("org.extendj.ast.AndLogicalExpr", 42, false),
        INSTANCE_OF_EXPR("org.extendj.ast.InstanceOfExpr", 43, false),
        CONDITIONAL_EXPR("org.extendj.ast.ConditionalExpr", 44, false),
        CAST_EXPR("org.extendj.ast.CastExpr", 45, false),
        NE_EXPR("org.extendj.ast.NEExpr", 46, false),
        ADD_EXPR("org.extendj.ast.AddExpr", 47, false),
        ASSIGN_PLUS_EXPR("org.extendj.ast.AssignPlusExpr", 48, false),
        ARRAY_TYPE_ACCESS("org.extendj.ast.ArrayTypeAccess", 49, false),
        INTEGER_LITERAL("org.extendj.ast.IntegerLiteral", 50, true),
        BOOLEAN_LITERAL("org.extendj.ast.BooleanLiteral", 51, true),
        PAR_EXPR("org.extendj.ast.ParExpr", 52, false),
        LE_EXPR("org.extendj.ast.LEExpr", 53, false),
        SUB_EXPR("org.extendj.ast.SubExpr", 54, false),
        GENERIC_METHOD_DECL("org.extendj.ast.GenericMethodDecl", 55, true),
        WILDCARD_SUPER("org.extendj.ast.WildcardSuper", 56, false),
        TYPE_VARIABLE("org.extendj.ast.TypeVariable", 57, true),
        MEMBER_INTERFACE_DECL("org.extendj.ast.MemberInterfaceDecl", 58, false),
        INTERFACE_DECL("org.extendj.ast.InterfaceDecl", 59, true),
        ELEMENT_VALUE_PAIR("org.extendj.ast.ElementValuePair", 60, true),
        ELEMENT_ARRAY_VALUE("org.extendj.ast.ElementArrayValue", 61, false),
        ELEMENT_CONSTANT_VALUE("org.extendj.ast.ElementConstantValue", 62, false),
        VARIABLE_ARITY_PARAMETER_DECLARATION("org.extendj.ast.VariableArityParameterDeclaration",
                63, true),
        LONG_LITERAL("org.extendj.ast.LongLiteral", 64, true),
        GENERIC_CONSTRUCTOR_DECL("org.extendj.ast.GenericConstructorDecl", 65, true),
        SUPER_CONSTRUCTOR_ACCESS("org.extendj.ast.SuperConstructorAccess", 66, false),
        GENERIC_CLASS_DECL("org.extendj.ast.GenericClassDecl", 67, true),
        ANONYMOUS_DECL("org.extendj.ast.AnonymousDecl", 68, true),
        CLASS_ACCESS("org.extendj.ast.ClassAccess", 69, false),
        ARRAY_CREATION_EXPR("org.extendj.ast.ArrayCreationExpr", 70, false),
        ARRAY_INIT("org.extendj.ast.ArrayInit", 71, false),
        ARRAY_TYPE_WITH_SIZE_ACCESS("org.extendj.ast.ArrayTypeWithSizeAccess", 72, false),
        DOUBLE_LITERAL("org.extendj.ast.DoubleLiteral", 73, true),
        FLOATING_POINT_LITERAL("org.extendj.ast.FloatingPointLiteral", 74, true),
        CHARACTER_LITERAL("org.extendj.ast.CharacterLiteral", 75, true),
        CONSTRUCTOR_ACCESS("org.extendj.ast.ConstructorAccess", 76, false),
        INSTANCE_INITIALIZER("org.extendj.ast.InstanceInitializer", 77, false),
        POST_INC_EXPR("org.extendj.ast.PostIncExpr", 78, false),
        LOG_NOT_EXPR("org.extendj.ast.LogNotExpr", 79, false),
        SUPER_ACCESS("org.extendj.ast.SuperAccess", 80, false),
        CONTINUE_STMT("org.extendj.ast.ContinueStmt", 81, true),
        ARRAY_ACCESS("org.extendj.ast.ArrayAccess", 82, false),
        FOR_STMT("org.extendj.ast.ForStmt", 83, false),
        LT_EXPR("org.extendj.ast.LTExpr", 84, false),
        OR_LOGICAL_EXPR("org.extendj.ast.OrLogicalExpr", 85, false),
        DIV_EXPR("org.extendj.ast.DivExpr", 86, false),
        ANNOTATION_DECL("org.extendj.ast.AnnotationDecl", 87, true),
        ANNOTATION_METHOD_DECL("org.extendj.ast.AnnotationMethodDecl", 88, true),
        ENUM_DECL("org.extendj.ast.EnumDecl", 89, true),
        ENUM_CONSTANT("org.extendj.ast.EnumConstant", 90, true),
        ENUM_INSTANCE_EXPR("org.extendj.ast.EnumInstanceExpr", 91, false),
        BOUND_FIELD_ACCESS("org.extendj.ast.BoundFieldAccess", 92, true),
        SWITCH_STMT("org.extendj.ast.SwitchStmt", 93, false),
        CONST_CASE("org.extendj.ast.ConstCase", 94, false),
        DEFAULT_CASE("org.extendj.ast.DefaultCase", 95, false),
        GT_EXPR("org.extendj.ast.GTExpr", 96, false),
        WHILE_STMT("org.extendj.ast.WhileStmt", 97, false),
        PRE_INC_EXPR("org.extendj.ast.PreIncExpr", 98, false),
        MUL_EXPR("org.extendj.ast.MulExpr", 99, false),
        BREAK_STMT("org.extendj.ast.BreakStmt", 100, true),
        GE_EXPR("org.extendj.ast.GEExpr", 101, false),
        POST_DEC_EXPR("org.extendj.ast.PostDecExpr", 102, false),
        EMPTY_STMT("org.extendj.ast.EmptyStmt", 103, false),
        DIMS("org.extendj.ast.Dims", 104, false),
        SYNCHRONIZED_STMT("org.extendj.ast.SynchronizedStmt", 105, false),
        STATIC_INITIALIZER("org.extendj.ast.StaticInitializer", 106, false),
        MINUS_EXPR("org.extendj.ast.MinusExpr", 107, false),
        ASSIGN_XOR_EXPR("org.extendj.ast.AssignXorExpr", 108, false),
        TYPE_IMPORT_ON_DEMAND_DECL("org.extendj.ast.TypeImportOnDemandDecl", 109, false),
        STATIC_IMPORT_ON_DEMAND_DECL("org.extendj.ast.StaticImportOnDemandDecl", 110, false),
        ASSERT_STMT("org.extendj.ast.AssertStmt", 111, false),
        LOCAL_CLASS_DECL_STMT("org.extendj.ast.LocalClassDeclStmt", 112, false),
        GENERIC_INTERFACE_DECL("org.extendj.ast.GenericInterfaceDecl", 113, true),
        DO_STMT("org.extendj.ast.DoStmt", 114, false),
        VARIABLE_DECLARATOR("org.extendj.ast.VariableDeclarator", 115, true),
        FIELD_DECL("org.extendj.ast.FieldDecl", 116, false),
        FIELD_DECLARATOR("org.extendj.ast.FieldDeclarator", 117, true),
        AND_BITWISE_EXPR("org.extendj.ast.AndBitwiseExpr", 118, false),
        ASSIGN_OR_EXPR("org.extendj.ast.AssignOrExpr", 119, false),
        ASSIGN_AND_EXPR("org.extendj.ast.AssignAndExpr", 120, false),
        BIT_NOT_EXPR("org.extendj.ast.BitNotExpr", 121, false),
        MOD_EXPR("org.extendj.ast.ModExpr", 122, false),
        ASSIGN_MINUS_EXPR("org.extendj.ast.AssignMinusExpr", 123, false),
        DIAMOND_ACCESS("org.extendj.ast.DiamondAccess", 124, false),
        TRY_WITH_RESOURCES("org.extendj.ast.TryWithResources", 125, false),
        RESOURCE_DECLARATION("org.extendj.ast.ResourceDeclaration", 126, true),
        RESOURCE_MODIFIERS("org.extendj.ast.ResourceModifiers", 127, false),
        PRE_DEC_EXPR("org.extendj.ast.PreDecExpr", 128, false),
        MULTI_CATCH("org.extendj.ast.MultiCatch", 129, false),
        CATCH_PARAMETER_DECLARATION("org.extendj.ast.CatchParameterDeclaration", 130, true),
        LABELED_STMT("org.extendj.ast.LabeledStmt", 131, true),
        ASSIGN_MUL_EXPR("org.extendj.ast.AssignMulExpr", 132, false),
        R_SHIFT_EXPR("org.extendj.ast.RShiftExpr", 133, false),
        XOR_BITWISE_EXPR("org.extendj.ast.XorBitwiseExpr", 134, false),
        L_SHIFT_EXPR("org.extendj.ast.LShiftExpr", 135, false),
        UR_SHIFT_EXPR("org.extendj.ast.URShiftExpr", 136, false),
        PLUS_EXPR("org.extendj.ast.PlusExpr", 137, false),
        ASSIGN_RSHIFT_EXPR("org.extendj.ast.AssignRShiftExpr", 138, false),
        ASSIGN_DIV_EXPR("org.extendj.ast.AssignDivExpr", 139, false),
        ASSIGN_MOD_EXPR("org.extendj.ast.AssignModExpr", 140, false),
        ASSIGN_URSHIFT_EXPR("org.extendj.ast.AssignURShiftExpr", 141, false),
        ASSIGN_LSHIFT_EXPR("org.extendj.ast.AssignLShiftExpr", 142, false),
        OR_BITWISE_EXPR("org.extendj.ast.OrBitwiseExpr", 143, false),
        ANNOTATED_COMPILATION_UNIT("org.extendj.ast.AnnotatedCompilationUnit", 144, true),
        LAMBDA_EXPR("org.extendj.ast.LambdaExpr", 145, false),
        DECLARED_LAMBDA_PARAMETERS("org.extendj.ast.DeclaredLambdaParameters", 146, false),
        EXPR_LAMBDA_BODY("org.extendj.ast.ExprLambdaBody", 147, false),
        BLOCK_LAMBDA_BODY("org.extendj.ast.BlockLambdaBody", 148, false),
        CLASS_REFERENCE("org.extendj.ast.ClassReference", 149, false),
        ELEMENT_ANNOTATION_VALUE("org.extendj.ast.ElementAnnotationValue", 150, false),

        ;

        public static final HashMap<Integer, ExtendjTypes> ENUMERATION_MAP = new HashMap<>();
        public static final HashMap<String, Integer> ID_MAP = new HashMap<>();
        public final String className;
        public final int id;
        public boolean hasValue;

        ExtendjTypes(String className, int id, boolean hasValue) {
            this.className = className;
            this.id = id;
            this.hasValue = hasValue;
        }

        /**
         * Gets the id.
         *
         * @param className the class name
         * @return the id
         */
        public static synchronized int getId(String className) {
            if (ID_MAP.isEmpty()) {
                for (ExtendjTypes fc : EnumSet.allOf(ExtendjTypes.class)) {
                    assert (!ENUMERATION_MAP.containsKey(fc.id));
                    ID_MAP.put(fc.className, fc.id);
                    ENUMERATION_MAP.put(fc.id, fc);
                }
            }
            Integer id = ID_MAP.get(className);
            if (id == null) {
                assert (false) : className;
                System.exit(-1);
            }
            return id;
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public static synchronized ExtendjTypes getTypeToId(int id) {
            if (ID_MAP.isEmpty()) {
                for (ExtendjTypes fc : EnumSet.allOf(ExtendjTypes.class)) {
                    ID_MAP.put(fc.className, fc.id);
                    ENUMERATION_MAP.put(fc.id, fc);
                }
            }
            ExtendjTypes type = ENUMERATION_MAP.get(id);
            if (type == null) {
                assert (false) : id;
            }
            return type;
        }
    }

    /**
     * Gets the type wrapped.
     *
     * @return the type wrapped
     */
    public int getTypeWrapped() {
        if (id == -1) {
            id = ExtendjTypes.getId(this.tree.getASTNode().getClass().getName());
        }
        return id;
    }

    @Override
    public boolean isLeaf() {
        return getChildren().size() == 0;
    }

    @Override
    public boolean isRoot() {
        assert (false);
        return false;
    }

    @Override
    public Iterable<ITree> preOrder() {
        return new Iterable<ITree>() {
            @Override
            public Iterator<ITree> iterator() {
                return TreeUtils.preOrderIterator(AstNodeArtifactWrapper.this);
            }
        };
    }

    @Override
    public Iterable<ITree> postOrder() {
        return new Iterable<ITree>() {
            @Override
            public Iterator<ITree> iterator() {
                return TreeUtils.postOrderIterator(AstNodeArtifactWrapper.this);
            }
        };
    }

    @Override
    public Iterable<ITree> breadthFirst() {
        assert (false);
        return null;
    }

    @Override
    public int positionInParent() {
        ITree parent = getParent();
        if (parent == null) {
            return -1;
        } else {
            return parent.getChildren().indexOf(this);
        }
    }

    @Override
    public void refresh() {
        assert (false);

    }

    @Override
    public void setChildren(List<ITree> children) {
        assert (false);

    }

    @Override
    public void setDepth(int depth) {
        assert (false);

    }

    @Override
    public void setHash(int hash) {
        this.hash = hash;

    }

    @Override
    public void setHeight(int height) {
        assert (false);

    }

    @Override
    public void setId(int id) {
        assert (false);

    }

    @Override
    public void setLabel(String label) {
        this.label = label;

    }

    @Override
    public void setLength(int length) {
        this.length = length;

    }

    @Override
    public void setParent(ITree parent) {
        this.parent = parent;

    }

    @Override
    public void setParentAndUpdateChildren(ITree parent) {
        this.parent = parent;

    }

    @Override
    public void setPos(int pos) {
        this.pos = pos;
    }

    @Override
    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public void setType(int type) {
        assert (false);

    }

    @Override
    public String toStaticHashString() {
        assert (false);
        return null;
    }

    @Override
    public String toShortString() {
        assert (false);
        return null;
    }

    @Override
    public String toTreeString() {
        assert (false);
        return null;
    }

    @Override
    public String toPrettyString(TreeContext ctx) {
        assert (false);
        return null;
    }

    @Override
    public Object getMetadata(String key) {
        assert (false);
        return null;
    }

    @Override
    public Iterator<Entry<String, Object>> getMetadata() {
        assert (false);
        return null;
    }

    @Override
    public Object setMetadata(String key, Object value) {
        assert (false);
        return null;
    }

    @Override
    public String toString() {
        return tree.toString();
    }

    @Override
    public void insertChild(ITree t, int position) {
        assert (false);
    }
    
    @Override
    public boolean hasSameType(ITree t) {
        assert (false);
        return false;
    }
    
    @Override
    public boolean isIsomorphicTo(ITree tree) {
        assert (false);
        return false;
    }
    
    @Override
    public boolean hasSameTypeAndLabel(ITree t) {
        assert (false);
        return false;
    }

}
