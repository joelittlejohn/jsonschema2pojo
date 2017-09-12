package com.mysema.scalagen

import japa.parser.ast.BlockComment
import japa.parser.ast.CompilationUnit
import japa.parser.ast.ImportDeclaration
import japa.parser.ast.LineComment
import japa.parser.ast.Node
import japa.parser.ast.PackageDeclaration
import japa.parser.ast.TypeParameter
import japa.parser.ast.body._
import japa.parser.ast.expr._
import japa.parser.ast.stmt._
import japa.parser.ast.`type`.ClassOrInterfaceType
import japa.parser.ast.`type`.PrimitiveType
import japa.parser.ast.`type`.ReferenceType
import japa.parser.ast.`type`.Type
import japa.parser.ast.`type`.VoidType
import japa.parser.ast.`type`.WildcardType
import japa.parser.ast.visitor.GenericVisitor
import java.util.{ArrayList, Collections}
import com.mysema.scalagen.ast.BeginClosureExpr

/**
 * 
 */
abstract class ModifierVisitor[A] extends GenericVisitor[Node, A] {
  
  protected def filter[T <: Node](node: T, arg: A): T = {
    if (node != null) node.accept(this, arg).asInstanceOf[T] else node
  } 
  
  protected def filter[T <: Node](list: JavaList[T], arg: A): JavaList[T]  = {
    if (list == null) {
      return null
    } else if (list.isEmpty) {
       Collections.emptyList[T]() 
    } else {
      //list.map(_.accept(this, arg).asInstanceOf[T]).filter(_ != null)
      val rv = new ArrayList[T](list.size)
      val it = list.iterator()
      while (it.hasNext) {
        val node = it.next().accept(this, arg).asInstanceOf[T]
        if (node != null) rv.add(node)
      }
      rv
    }    
  }
  
  def visitName(name: String, arg: A) = name
  
  def visit(n: AnnotationDeclaration, arg: A) : Node = {
    val rv = new AnnotationDeclaration()
    rv.setAnnotations(filter(n.getAnnotations, arg))
    rv.setJavaDoc(filter(n.getJavaDoc, arg))    
    rv.setMembers(filter(n.getMembers, arg))
    rv.setModifiers(n.getModifiers)
    rv.setName(n.getName)
    rv    
  }

  def visit(n: AnnotationMemberDeclaration, arg: A): Node = {
    val rv = new AnnotationMemberDeclaration()
    rv.setAnnotations(filter(n.getAnnotations, arg))
    rv.setDefaultValue(filter(n.getDefaultValue, arg))
    rv.setJavaDoc(filter(n.getJavaDoc, arg))
    rv.setModifiers(n.getModifiers)
    rv.setName(n.getName)
    rv.setType(filter(n.getType, arg))
    
    n
  }

  def visit(n: ArrayAccessExpr, arg: A): Node = {
    val rv = new ArrayAccessExpr()
    rv.setIndex(filter(n.getIndex, arg))
    rv.setName(filter(n.getName, arg))    
    rv
  }

  def visit(n: ArrayCreationExpr, arg: A): Node = {
    val rv = new ArrayCreationExpr()    
    rv.setArrayCount(n.getArrayCount)
    rv.setDimensions(filter(n.getDimensions, arg))
    rv.setInitializer(filter(n.getInitializer, arg))
    rv.setType(filter(n.getType, arg))   
    rv
  }

  def visit(n: ArrayInitializerExpr, arg: A): Node = {
    val rv = new ArrayInitializerExpr()
    rv.setValues(filter(n.getValues, arg))   
    rv
  }

  def visit(n: AssertStmt, arg: A): Node = {
    val rv = new AssertStmt()
    rv.setCheck(filter(n.getCheck, arg))
    rv.setMessage(filter(n.getMessage, arg))    
    rv
  }

  def visit(n: AssignExpr, arg: A): Node = {
    val rv = new AssignExpr()
    rv.setOperator(n.getOperator)
    rv.setTarget(filter(n.getTarget, arg))
    rv.setValue(filter(n.getValue, arg))    
    rv
  }

  def visit(n: BinaryExpr, arg: A): Node = {
    val rv = new BinaryExpr()
    rv.setOperator(n.getOperator)
    rv.setLeft(filter(n.getLeft, arg))
    rv.setRight(filter(n.getRight, arg))    
    rv
  }

  def visit(n: BlockStmt, arg: A): Node = new BlockStmt(filter(n.getStmts, arg))

  def visit(n: BooleanLiteralExpr, arg: A): Node = new BooleanLiteralExpr(n.getValue)

  def visit(n: BreakStmt, arg: A): Node = new BreakStmt(n.getId)      

  def visit(n: CastExpr, arg: A): Node = {
    new CastExpr(filter(n.getType, arg), filter(n.getExpr, arg))    
  }

  def visit(n: CatchClause, arg: A): Node = {
    new CatchClause(filter(n.getExcept, arg), filter(n.getCatchBlock, arg))
  }

  def visit(n: CharLiteralExpr, arg: A): Node = new CharLiteralExpr(n.getValue)

  def visit(n: ClassExpr, arg: A): Node = new ClassExpr(filter(n.getType, arg))

  def visit(n: ClassOrInterfaceDeclaration, arg: A): Node = {
    val rv = new ClassOrInterfaceDeclaration()
    rv.setAnnotations(filter(n.getAnnotations, arg))
    rv.setExtends(filter(n.getExtends, arg))
    rv.setImplements(filter(n.getImplements, arg))
    rv.setInterface(n.isInterface)
    rv.setJavaDoc(filter(n.getJavaDoc, arg))
    rv.setMembers(filter(n.getMembers, arg))
    rv.setModifiers(n.getModifiers)
    rv.setName(n.getName)
    rv.setTypeParameters(filter(n.getTypeParameters, arg))        
    rv
  }

  def visit(n: ClassOrInterfaceType, arg: A): Node = {
    val rv = new ClassOrInterfaceType()
    rv.setName(n.getName)
    rv.setScope(filter(n.getScope, arg))
    rv.setTypeArgs(filter(n.getTypeArgs, arg))
    rv
  }

  def visit(n: CompilationUnit, arg: A): Node = {
    val rv = new CompilationUnit()
    rv.setPackage(filter(n.getPackage, arg))
    rv.setImports(filter(n.getImports, arg))
    rv.setTypes(filter(n.getTypes, arg))
    rv
  }

  def visit(n: ConditionalExpr, arg: A): Node = {
    val rv = new ConditionalExpr()
    rv.setCondition(filter(n.getCondition, arg))
    rv.setThenExpr(filter(n.getThenExpr, arg))
    rv.setElseExpr(filter(n.getElseExpr, arg))
    rv
  }

  def visit(n: ConstructorDeclaration, arg: A): Node = {
    val rv = new ConstructorDeclaration()
    rv.setAnnotations(filter(n.getAnnotations, arg))
    rv.setBlock(filter(n.getBlock, arg))
    rv.setJavaDoc(filter(n.getJavaDoc, arg))
    rv.setModifiers(n.getModifiers)
    rv.setName(n.getName)
    rv.setParameters(filter(n.getParameters, arg))
    rv.setThrows(filter(n.getThrows, arg))
    rv.setTypeParameters(filter(n.getTypeParameters, arg))
    rv
  }

  def visit(n: ContinueStmt, arg: A): Node = new ContinueStmt(n.getId)      

  def visit(n: DoStmt, arg: A): Node = {
    val rv = new DoStmt()
    rv.setBody(filter(n.getBody, arg))
    rv.setCondition(filter(n.getCondition, arg))
    rv
  }

  def visit(n: DoubleLiteralExpr, arg: A): Node = new DoubleLiteralExpr(n.getValue)

  def visit(n: EmptyMemberDeclaration, arg: A): Node = {
    new EmptyMemberDeclaration(filter(n.getJavaDoc, arg))
  }

  def visit(n: EmptyStmt, arg: A): Node = new EmptyStmt()

  def visit(n: EmptyTypeDeclaration, arg: A): Node = {
    new EmptyTypeDeclaration(filter(n.getJavaDoc, arg))
  }

  def visit(n: EnclosedExpr, arg: A): Node = {
    new EnclosedExpr(filter(n.getInner, arg))
  }

  def visit(n: EnumConstantDeclaration, arg: A): Node = {
    val rv = new EnumConstantDeclaration()
    rv.setAnnotations(filter(n.getAnnotations, arg))
    rv.setArgs(filter(n.getArgs, arg))
    rv.setClassBody(filter(n.getClassBody, arg))
    rv.setJavaDoc(filter(n.getJavaDoc, arg))
    rv.setName(n.getName)
    rv
  }

  def visit(n: EnumDeclaration, arg: A): Node = {
    val rv = new EnumDeclaration()
    rv.setAnnotations(filter(n.getAnnotations, arg))
    rv.setEntries(filter(n.getEntries, arg))
    rv.setImplements(filter(n.getImplements, arg))
    rv.setJavaDoc(filter(n.getJavaDoc, arg))
    rv.setMembers(filter(n.getMembers, arg))
    rv.setModifiers(n.getModifiers)
    rv.setName(n.getName)    
    rv
  }

  def visit(n: ExplicitConstructorInvocationStmt, arg: A): Node = {
    val rv = new ExplicitConstructorInvocationStmt()
    rv.setArgs(filter(n.getArgs, arg))
    rv.setExpr(filter(n.getExpr, arg))
    rv.setThis(n.isThis)    
    rv.setTypeArgs(filter(n.getTypeArgs, arg))
    rv
  }

  def visit(n: ExpressionStmt, arg: A): Node = {
    new ExpressionStmt(filter(n.getExpression, arg))
  }

  def visit(n: FieldAccessExpr, arg: A): Node = {
    new FieldAccessExpr(filter(n.getScope, arg), visitName(n.getField, arg))
  }

  def visit(n: FieldDeclaration, arg: A): Node = {
    val rv = new FieldDeclaration()
    rv.setAnnotations(filter(n.getAnnotations, arg))
    rv.setJavaDoc(filter(n.getJavaDoc, arg))
    rv.setModifiers(n.getModifiers)
    rv.setType(filter(n.getType, arg))
    rv.setVariables(filter(n.getVariables, arg))
    rv
  }

  def visit(n: ForeachStmt, arg: A): Node = {
    val rv = new ForeachStmt()
    rv.setVariable(filter(n.getVariable, arg))
    rv.setIterable(filter(n.getIterable, arg))
    rv.setBody(filter(n.getBody, arg))
    rv
  }

  def visit(n: ForStmt, arg: A): Node = {
    val rv = new ForStmt()
    rv.setInit(filter(n.getInit, arg))
    rv.setCompare(filter(n.getCompare, arg))
    rv.setUpdate(filter(n.getUpdate, arg))
    rv.setBody(filter(n.getBody, arg))
    rv
  }

  def visit(n: IfStmt, arg: A): Node = {
    val rv = new IfStmt()
    rv.setCondition(filter(n.getCondition, arg))
    rv.setThenStmt(filter(n.getThenStmt, arg))
    rv.setElseStmt(filter(n.getElseStmt, arg))
    rv
  }

  def visit(n: ImportDeclaration, arg: A): Node = {
    new ImportDeclaration(n.getName, n.isStatic, n.isAsterisk)    
  }

  def visit(n: InitializerDeclaration, arg: A): Node = {
    val rv = new InitializerDeclaration()
    rv.setAnnotations(filter(n.getAnnotations, arg))
    rv.setBlock(filter(n.getBlock, arg))
    rv.setJavaDoc(filter(n.getJavaDoc, arg))    
    rv.setStatic(n.isStatic)
    rv
  }

  def visit(n: InstanceOfExpr, arg: A): Node = {
    val rv = new InstanceOfExpr()
    rv.setExpr(filter(n.getExpr, arg))
    rv.setType(filter(n.getType, arg))
    rv
  }

  def visit(n: IntegerLiteralExpr, arg: A): Node = new IntegerLiteralExpr(n.getValue)

  def visit(n: IntegerLiteralMinValueExpr, arg: A): Node = new IntegerLiteralMinValueExpr()

  def visit(n: JavadocComment, arg: A): Node = new JavadocComment(n.getContent)

  def visit(n: LabeledStmt, arg: A): Node = new LabeledStmt(n.getLabel, filter(n.getStmt, arg))    

  def visit(n: LongLiteralExpr, arg: A): Node = new LongLiteralExpr(n.getValue)

  def visit(n: LongLiteralMinValueExpr, arg: A): Node = new LongLiteralMinValueExpr()

  def visit(n: MarkerAnnotationExpr, arg: A): Node = {
    new MarkerAnnotationExpr(filter(n.getName, arg))
  } 

  def visit(n: MemberValuePair, arg: A): Node = {
    new MemberValuePair(n.getName, filter(n.getValue, arg))
  }

  def visit(n: MethodCallExpr, arg: A): Node = {
    val rv = new MethodCallExpr()
    rv.setArgs(filter(n.getArgs, arg))
    rv.setName(n.getName)
    rv.setScope(filter(n.getScope, arg))
    rv.setTypeArgs(filter(n.getTypeArgs, arg))
    rv
  }

  def visit(n: MethodDeclaration, arg: A): Node = {
    val rv = new MethodDeclaration()
    rv.setAnnotations(filter(n.getAnnotations, arg))
    rv.setArrayCount(n.getArrayCount)
    rv.setBody(filter(n.getBody, arg))
    rv.setJavaDoc(filter(n.getJavaDoc, arg))
    rv.setModifiers(n.getModifiers)
    rv.setName(n.getName)
    rv.setParameters(filter(n.getParameters, arg))
    rv.setThrows(filter(n.getThrows, arg))
    rv.setType(filter(n.getType, arg))
    rv.setTypeParameters(filter(n.getTypeParameters, arg))
    rv
  }

  def visit(n: NameExpr, arg: A): Node = n match {
    case closure: BeginClosureExpr => closure
    case _ => new NameExpr(visitName(n.getName, arg))
  }

  def visit(n: NormalAnnotationExpr, arg: A): Node = {
    val rv = new NormalAnnotationExpr()
    rv.setName(filter(n.getName, arg))
    rv.setPairs(filter(n.getPairs, arg))
    rv
  }

  def visit(n: NullLiteralExpr, arg: A): Node = new NullLiteralExpr()

  def visit(n: ObjectCreationExpr, arg: A): Node = {
    val rv = new ObjectCreationExpr()
    rv.setAnonymousClassBody(filter(n.getAnonymousClassBody, arg))
    rv.setArgs(filter(n.getArgs, arg))
    rv.setScope(filter(n.getScope, arg))
    rv.setType(filter(n.getType, arg))
    rv.setTypeArgs(filter(n.getTypeArgs, arg))
    rv
  }

  def visit(n: PackageDeclaration, arg: A): Node = {
    val rv = new PackageDeclaration()
    rv.setAnnotations(filter(n.getAnnotations, arg))
    rv.setName(filter(n.getName, arg))
    rv    
  }
  
  def visit(n: Parameter, arg: A): Node = {
    val rv = new Parameter()
    visit(n, rv, arg)
    rv.setType(filter(n.getType, arg))
    rv.setVarArgs(n.isVarArgs)
    rv
  }
  
  def visit(n: MultiTypeParameter, arg: A): Node = {
    val rv = new MultiTypeParameter()
    visit(n, rv, arg)
    rv.setTypes(n.getTypes().map(tpe => filter(tpe, arg)))
    rv
  }

  def visit(n: BaseParameter, rv: BaseParameter, arg: A): Node = {
    rv.setAnnotations(filter(n.getAnnotations, arg))
    rv.setId(filter(n.getId, arg))
    rv.setModifiers(n.getModifiers)
    rv
  }

  def visit(n: PrimitiveType, arg: A): Node = new PrimitiveType(n.getType)      

  def visit(n: QualifiedNameExpr, arg: A): Node = {
    val rv = new QualifiedNameExpr()
    rv.setName(n.getName)
    rv.setQualifier(filter(n.getQualifier, arg))
    rv
  }

  def visit(n: ReferenceType, arg: A): Node = {
    new ReferenceType(filter(n.getType, arg), n.getArrayCount)
  }

  def visit(n: ReturnStmt, arg: A): Node = {
    new ReturnStmt(filter(n.getExpr, arg))
  }

  def visit(n: SingleMemberAnnotationExpr, arg: A): Node = {
    new SingleMemberAnnotationExpr(filter(n.getName, arg), filter(n.getMemberValue, arg))
  }

  def visit(n: StringLiteralExpr, arg: A): Node = new StringLiteralExpr(n.getValue)

  def visit(n: SuperExpr, arg: A): Node = {
    new SuperExpr(filter(n.getClassExpr, arg))
  }

  def visit(n: SwitchEntryStmt, arg: A): Node = {
    val rv = new SwitchEntryStmt()
    rv.setLabel(filter(n.getLabel, arg))
    rv.setStmts(filter(n.getStmts, arg))
    rv
  }

  def visit(n: SwitchStmt, arg: A): Node = {
    val rv = new SwitchStmt()
    rv.setSelector(filter(n.getSelector, arg))
    rv.setEntries(filter(n.getEntries, arg))
    rv
  }

  def visit(n: SynchronizedStmt, arg: A): Node = {
    val rv = new SynchronizedStmt() 
    rv.setExpr(filter(n.getExpr, arg))
    rv.setBlock(filter(n.getBlock, arg))
    rv
  }

  def visit(n: ThisExpr, arg: A): Node = {
    new ThisExpr(filter(n.getClassExpr, arg))
  }

  def visit(n: ThrowStmt, arg: A): Node = {
    new ThrowStmt(filter(n.getExpr, arg))
  }

  def visit(n: TryStmt, arg: A): Node = {
    val rv = new TryStmt()
    rv.setResources(filter(n.getResources, arg))
    rv.setTryBlock(filter(n.getTryBlock, arg))
    rv.setCatchs(filter(n.getCatchs, arg))
    rv.setFinallyBlock(filter(n.getFinallyBlock, arg))
    rv
  }

  def visit(n: TypeDeclarationStmt, arg: A): Node = {
    new TypeDeclarationStmt(filter(n.getTypeDeclaration, arg))
  }

  def visit(n: TypeParameter, arg: A): Node = {
    new TypeParameter(n.getName, filter(n.getTypeBound, arg))
  }

  def visit(n: UnaryExpr, arg: A): Node = {
    new UnaryExpr(filter(n.getExpr, arg), n.getOperator)    
  }

  def visit(n: VariableDeclarationExpr, arg: A): Node = {
    val rv = new VariableDeclarationExpr()
    rv.setAnnotations(filter(n.getAnnotations, arg))
    rv.setModifiers(n.getModifiers)
    rv.setType(filter(n.getType, arg))
    rv.setVars(filter(n.getVars, arg))
    rv
  }

  def visit(n: VariableDeclarator, arg: A): Node = {
    new VariableDeclarator(filter(n.getId, arg), filter(n.getInit, arg))
  }

  def visit(n: VariableDeclaratorId, arg: A): Node = {
    val rv = new VariableDeclaratorId()
    rv.setArrayCount(n.getArrayCount)
    rv.setName(visitName(n.getName, arg))
    rv
  }

  def visit(n: VoidType, arg: A): Node = new VoidType()

  def visit(n: WhileStmt, arg: A): Node = {
    val rv = new WhileStmt()
    rv.setCondition(filter(n.getCondition, arg))
    rv.setBody(filter(n.getBody, arg))
    rv
  }

  def visit(n: WildcardType, arg: A): Node = {
    val rv = new WildcardType()
    rv.setExtends(filter(n.getExtends, arg))
    rv.setSuper(filter(n.getSuper, arg))
    rv
  }

  def visit(n: BlockComment, arg: A): Node = new BlockComment(n.getContent)

  def visit(n: LineComment, arg: A): Node = new LineComment(n.getContent)
  
}
