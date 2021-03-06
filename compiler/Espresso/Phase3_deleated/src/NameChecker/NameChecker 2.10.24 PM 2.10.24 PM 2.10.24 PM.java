package NameChecker;

import AST.*;
import Utilities.Error;
import Utilities.SymbolTable;
import Utilities.Visitor;
import Utilities.Rewrite;

import java.util.*;
import Parser.*;;

public class NameChecker extends Visitor {

	/* getMethod traverses the class hierarchy to look for a method of
       name 'methodName'. We return #t if we find any method with the
       correct name. Since we don't have types yet we cannot look at
       the signature of the method, so all we do for now is look if
       any method is defined. The search is as follows:

       1) look in the current class
       2) look in its super class
       3) look in all the interfaces

       Remember that the an entry in the methodTable is a symbol table
       it self. It holds all entries of the same name, but with
       different signatures. (See Documentation)
	 */    
	public static SymbolTable getMethod(String methodName, ClassDecl cd) {

		// YOUR CODE HERE
		return null;
	}

	/* Same as getMethod just for fields instead 
	 */
	public static AST getField(String fieldName, ClassDecl cd) {

		// YOUR CODE HERE
		return null;
	}

	/* Traverses all the classes and interfaces and builds a sequence
	   of the methods and constructors of the class hierarchy.
	 */
    public void getClassHierarchyMethods(ClassDecl cd, Sequence lst, Hashtable<String, Object> seenClasses) {
		// YOUR CODE HERE
    }

	/* For each method (not constructors) in this list, check that if
       it exists more than once with the same parameter signature that
       they all return something of the same type. 
	*/
    public void checkReturnTypesOfIdenticalMethods(Sequence lst) {
	// YOUR CODE HERE
    }
    
    /* Divides all the methods into two sequences: one for all the
       abstract ones, and one for all the concrete ones and check that
       all the methods that were delcared abstract were indeed
       implemented somewhere in the class hierarchy.  */
    

    // sup is the class in which the abstract method lives,
    // sub is the class in which the concrete method lives.
    public static boolean isSuper(ClassDecl sup, ClassDecl sub) {
	if (sup.name().equals(sub.name()))
	    return true;
		
	if (sub.superClass() != null && isSuper(sup, sub.superClass().myDecl))
	    return true;

	for (int i=0; i<sub.interfaces().nchildren; i++) 
	    if (isSuper(sup, ((ClassType)sub.interfaces().children[i]).myDecl))
		return true;

	return false;
    }


       public void checkImplementationOfAbstractClasses(ClassDecl cd, Sequence methods) {
	   // YOUR CODE HERE
       }
    
	public  void checkUniqueFields(Sequence fields, ClassDecl cd) {
		// YOUR CODE HERE
	}

	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
	// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

	/**
	 * Points to the current scope.
	 */
	private SymbolTable currentScope;
	/**
	 * The class table<br>
	 * This is set in the constructor.
	 */
	private SymbolTable classTable;
	/**
	 * The current class in which we are working.<br>
	 * This is set in visitClassDecl().
	 */
	private ClassDecl   currentClass;
	
	public NameChecker(SymbolTable classTable, boolean debug) { 
		this.classTable = classTable; 
		this.debug = debug;
	}

	/** BLOCK */
	public Object visitBlock(Block bl) {
		println("Block:\t\t Creating new scope for Block.");
		currentScope = currentScope.newScope();
		super.visitBlock(bl);
		currentScope = currentScope.closeScope(); 
		return null;
	}

		/** FORSTAT */
	public Object visitForStat(ForStat fs) {
		println("ForStat:\t Creating new scope for For Statement.");
		currentScope = currentScope.newScope();
		super.visitForStat(fs);
		currentScope = currentScope.closeScope(); 
		return null;
	}

	// for visitMethodDecl


	public Object visitMethodDecl(MethodDecl mtd) {
	println("MethodDecl:\t Creating new scope for Method '" + mtd.name() +"' with signature '" + mtd.paramSignature()  +"' (Parameters and Locals)." );
	currentScope = currentScope.newScope();
	mtd.params().visit(this);
	mtd.block().visit(this);
	currentScope = currentScope.closeScope(); 
	return null;
	}

	//for constructor decl


     public Object visitConstructorDecl(ConstructorDecl cdl) 
     { 
     	println("ConstructorDecl: Creating new scope for constructor <init> with signature '" + cdl.paramSignature()+ "' (Parameters and Locals).");
     	currentScope = currentScope.newScope();

     	cdl.params().visit(this); 
     	currentScope = currentScope.newScope(); 
     	if (cdl.cinvocation() != null)
     	    {
     	    	cdl.cinvocation().visit(this); 
     	    }
        cdl.body().visit(this); 
        currentScope = currentScope.closeScope();
         currentScope = currentScope.closeScope();
          return null; 
      }

      /** visitLocalDecl */

      public Object visitLocalDecl(LocalDecl ldl) 
	  {
	     	println("LocalDecl:\t Declaring local symbol '" +ldl.name()+ "'.");
	     	ldl.visitChildren(this);
            currentScope.put(ldl.name(), ldl);
	     	return null;

	   }
     
     public Object visitParamDecl(ParamDecl pdl) 
     {
     	println("ParamDecl:\t Declaring local symbol '" +pdl.name()+ "'.");


        /*** insert parameter in to current scope ***/
        pdl.visitChildren(this);
        currentScope.put(pdl.name(), pdl);


     	return null;

     }

      public Object visitNameExpr(NameExpr ne) 
      {
     	println("NameExpr:\t Looking up symbol '" +ne.name()+ "'.");

     	/*search the current scope*/
     	(SymbolTable) symbol = (SymbolTable)currentScope.get(ne.name);

     	if(symbol = null)
     	{
     		/* search fieldtable of current class */
           (SymbolTable) symbol = (SymbolTable)currentClass.fieldTable.get(ne.name);
           

           if (symbol =  null)
           {
           	/*search in global class table */
           	symbol = classTable.get(ne.name);
           }




     	}


     	// if(current_name = null)
     	// {
     	// 	search_field = currentC
     	// }
     	return null;

      }
     	
      public Object visitClassType(ClassType ct) {
		ClassDecl cd = (ClassDecl) classTable.get(ct.typeName());

		println("ClassType:\t Looking up class/interface '" + ct.typeName() + "' in class table.");

		if (cd == null) 
			Error.error(ct,"Class '" + ct.typeName() + "' not found.");
		ct.myDecl = cd;
		return null;
	}



	 // Creating new scope for Method 'main' with signature '' (Parameters and Locals).


	/** CLASS DECLARATION */
	public Object visitClassDecl(ClassDecl cd) {
		println("ClassDecl:\t Visiting class '"+cd.name()+"'");

		// If we use the field table here as the top scope, then we do not
		// need to look in the field table when we resolve NameExpr. Note,
		// at this time we have not yet rewritten NameExprs which are really
		// FieldRefs with a null target as we have not resolved anything yet.
		currentScope = cd.fieldTable;
		currentClass = cd;

		Hashtable<String, Object> seenClasses = new Hashtable<String, Object>();

		// Check that the superclass is a class.
		if (cd.superClass() != null) 
			if (cd.superClass().myDecl.isInterface())
				Error.error(cd,"Class '" + cd.name() + "' cannot inherit from interface '" +
						cd.superClass().myDecl.name() + "'.");



		if (cd.superClass() != null) {
			if (cd.name().equals(cd.superClass().typeName()))
				Error.error(cd, "Class '" + cd.name() + "' cannot extend itself.");
			// If a superclass has a private default constructor, the 
			// class cannot be extended.
			ClassDecl superClass = (ClassDecl)classTable.get(cd.superClass().typeName());
			SymbolTable st = (SymbolTable)superClass.methodTable.get("<init>");
			ConstructorDecl ccd = (ConstructorDecl)st.get("");
			if (ccd != null && ccd.getModifiers().isPrivate())
			    Error.error(cd, "Class '" + superClass.className().getname() + "' cannot be extended because it has a private default constructor.");
		}
		
		// Visit the children
		super.visitClassDecl(cd);
			
		currentScope = null;

		// Check that the interfaces implemented are interfaces.
		for (int i=0; i<cd.interfaces().nchildren; i++) {
			ClassType ct = (ClassType)cd.interfaces().children[i];
			if (ct.myDecl.isClass())
				Error.error(cd,"Class '" + cd.name() + "' cannot implement class '" + ct.name() + "'.");
		}

		Sequence methods = new Sequence();
		
		getClassHierarchyMethods(cd, methods, seenClasses);

		checkReturnTypesOfIdenticalMethods(methods);

		// If the class is not abstract and not an interface it must implement all
		// the abstract functions of its superclass(es) and its interfaces.
		if (!cd.isInterface() && !cd.modifiers.isAbstract()) {
			checkImplementationOfAbstractClasses(cd, methods);
			// checkImplementationOfAbstractClasses(cd, new Sequence());
		}
		// All field names can only be used once in a class hierarchy
		checkUniqueFields(new Sequence(), cd);

		cd.allMethods = methods; // now contains only MethodDecls

		// Fill cd.constructors.
		SymbolTable st = (SymbolTable)cd.methodTable.get("<init>");
		ConstructorDecl cod;
		if (st != null) {
			for (Enumeration<Object> e = st.entries.elements() ; 
					e.hasMoreElements(); ) {
				cod = (ConstructorDecl)e.nextElement();
				cd.constructors.append(cod);
			}
		}

		// needed for rewriting the tree to replace field references
		// represented by NameExpr.
		println("ClassDecl:\t Performing tree Rewrite on " + cd.name());
		new Rewrite().go(cd, cd);

		return null;
	}
	// YOUR CODE HERE

	/** THIS */
	public Object visitThis(This th) {
		ClassType ct = new ClassType(new Name(new Token(16,currentClass.name(),0,0,0)));
		ct.myDecl = currentClass;
		th.type = ct;
		return null;
	}

}

