package java_cup.runtime;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;



public abstract class lr_parser {

    //todo java_cup_v10k.tar.gz src code http://www2.cs.tum.edu/projects/cup/releases/ https://www2.in.tum.de/repos/cup/develop/

    /**
     * Simple constructor.
     */
    public lr_parser() {
        /* nothing to do here */
    }

    /**
     * Constructor that sets the default scanner. [CSA/davidm]
     */
    public lr_parser(Scanner s) {
        this(); /* in case default constructor someday does something */
        setScanner(s);
    }

    /*-----------------------------------------------------------*/
    /*--- (Access to) Static (Class) Variables ------------------*/
    /*-----------------------------------------------------------*/

    /**
     * The default number of Symbols after an error we much match to consider
     * it recovered from.
     */
    protected final static int _error_sync_size = 3;

    /**
     * The number of Symbols after an error we much match to consider it
     * recovered from.
     */
    protected int error_sync_size() {
        return _error_sync_size;
    }

    /*-----------------------------------------------------------*/
    /*--- (Access to) Instance Variables ------------------------*/
    /*-----------------------------------------------------------*/

    /**
     * Table of production information (supplied by generated subclass).
     * This table contains one entry per production and is indexed by
     * the negative-encoded values (reduce actions) in the action_table.
     * Each entry has two parts, the index of the non-terminal on the
     * left hand side of the production, and the number of Symbols
     * on the right hand side.
     */
    public abstract short[][] production_table();

    /**
     * The action table (supplied by generated subclass).  This table is
     * indexed by state and terminal number indicating what action is to
     * be taken when the parser is in the given state (i.e., the given state
     * is on top of the stack) and the given terminal is next on the input.
     * States are indexed using the first dimension, however, the entries for
     * a given state are compacted and stored in adjacent index, value pairs
     * which are searched for rather than accessed directly (see get_action()).
     * The actions stored in the table will be either shifts, reduces, or
     * errors.  Shifts are encoded as positive values (one greater than the
     * state shifted to).  Reduces are encoded as negative values (one less
     * than the production reduced by).  Error entries are denoted by zero.
     *
     * @see java_cup.runtime.lr_parser#get_action
     */
    public abstract short[][] action_table();

    /**
     * The reduce-goto table (supplied by generated subclass).  This
     * table is indexed by state and non-terminal number and contains
     * state numbers.  States are indexed using the first dimension, however,
     * the entries for a given state are compacted and stored in adjacent
     * index, value pairs which are searched for rather than accessed
     * directly (see get_reduce()).  When a reduce occurs, the handle
     * (corresponding to the RHS of the matched production) is popped off
     * the stack.  The new top of stack indicates a state.  This table is
     * then indexed by that state and the LHS of the reducing production to
     * indicate where to "shift" to.
     *
     * @see java_cup.runtime.lr_parser#get_reduce
     */
    public abstract short[][] reduce_table();

    /**
     * The index of the start state (supplied by generated subclass).
     */
    public abstract int start_state();

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /**
     * The index of the start production (supplied by generated subclass).
     */
    public abstract int start_production();

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /**
     * The index of the end of file terminal Symbol (supplied by generated
     * subclass).
     */
    public abstract int EOF_sym();

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /**
     * The index of the special error Symbol (supplied by generated subclass).
     */
    public abstract int error_sym();

    /**
     * Internal flag to indicate when parser should quit.
     */
    protected boolean _done_parsing = false;

    /**
     * This method is called to indicate that the parser should quit.  This is
     * normally called by an accept action, but can be used to cancel parsing
     * early in other circumstances if desired.
     */
    public void done_parsing() {
        _done_parsing = true;
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/
    /* Global parse state shared by parse(), error recovery, and
     * debugging routines */
    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /**
     * Indication of the index for top of stack (for use by actions).
     */
    protected int tos;

    /**
     * The current lookahead Symbol.
     */
    protected Symbol cur_token;


    /**
     * The parse stack itself.
     */
    protected Stack stack = new Stack();

    /**
     * Direct reference to the production table.
     */
    protected short[][] production_tab;

    /**
     * Direct reference to the action table.
     */
    protected short[][] action_tab;

    /**
     * Direct reference to the reduce-goto table.
     */
    protected short[][] reduce_tab;

    /**
     * This is the scanner object used by the default implementation
     * of scan() to get Symbols.  To avoid name conflicts with existing
     * code, this field is private. [CSA/davidm]
     */
    private Scanner _scanner;

    /**
     * Simple accessor method to set the default scanner.
     */
    public void setScanner(Scanner s) {
        _scanner = s;
    }

    /**
     * Simple accessor method to get the default scanner.
     */
    public Scanner getScanner() {
        return _scanner;
    }

    /*-----------------------------------------------------------*/
    /*--- General Methods ---------------------------------------*/
    /*-----------------------------------------------------------*/

    /**
     * Perform a bit of user supplied action code (supplied by generated
     * subclass).  Actions are indexed by an internal action number assigned
     * at parser generation time.
     *
     * @param act_num the internal index of the action to be performed.
     * @param parser  the parser object we are acting for.
     * @param stack   the parse stack of that object.
     * @param top     the index of the top element of the parse stack.
     */
    public abstract Symbol do_action(
            int act_num,
            lr_parser parser,
            Stack stack,
            int top)
            throws Exception;


    /**
     * User code for initialization inside the parser.  Typically this
     * initializes the scanner.  This is called before the parser requests
     * the first Symbol.  Here this is just a placeholder for subclasses that
     * might need this and we perform no action.   This method is normally
     * overridden by the generated code using this contents of the "init with"
     * clause as its body.
     */
    public void user_init() throws Exception {
    }

    /**
     * Initialize the action object.  This is called before the parser does
     * any parse actions. This is filled in by generated code to create
     * an object that encapsulates all action code.
     */
    protected abstract void init_actions() throws java.lang.Exception;

    /**
     * Get the next Symbol from the input (supplied by generated subclass).
     * Once end of file has been reached, all subsequent calls to scan
     * should return an EOF Symbol (which is Symbol number 0).  By default
     * this method returns getScanner().next_token(); this implementation
     * can be overriden by the generated parser using the code declared in
     * the "scan with" clause.  Do not recycle objects; every call to
     * scan() should return a fresh object.
     */
    public Symbol scan() throws Exception {
        Symbol sym = getScanner().next_token();
        return (sym != null) ? sym : new Symbol(EOF_sym());
    }

    /**
     * Report a fatal error.  This method takes a  message string and an
     * additional object (to be used by specializations implemented in
     * subclasses).  Here in the base class a very simple implementation
     * is provided which reports the error then throws an exception.
     *
     * @param message an error message.
     * @param info    an extra object reserved for use by specialized subclasses.
     */
    public void report_fatal_error(String message, Object info) throws Exception {
        /* stop parsing (not really necessary since we throw an exception, but) */
        done_parsing();

        /* use the normal error message reporting to put out the message */
        report_error(message, info);

        /* throw an exception */
        throw new Exception("Can't recover from previous error(s)");
    }

    /**
     * Report a non fatal error (or warning).  This method takes a message
     * string and an additional object (to be used by specializations
     * implemented in subclasses).  Here in the base class a very simple
     * implementation is provided which simply prints the message to
     * System.err.
     *
     * @param message an error message.
     * @param info    an extra object reserved for use by specialized subclasses.
     */
    public void report_error(String message, Object info) {
        System.err.print(message);
        if (info instanceof Symbol) {
            if (((Symbol) info).left != -1) {
                System.err.println(" at character " + ((Symbol) info).left +
                        " of input");
            } else {
                System.err.println("");
            }
        } else {
            System.err.println("");
        }

    }

    /**
     * This method is called when a syntax error has been detected and recovery
     * is about to be invoked.  Here in the base class we just emit a
     * "Syntax error" error message.
     *
     * @param cur_token the current lookahead Symbol.
     */
    public void syntax_error(Symbol cur_token) {
        report_error("Syntax error", cur_token);
    }

    /**
     * This method is called if it is determined that syntax error recovery
     * has been unsuccessful.  Here in the base class we report a fatal error.
     *
     * @param cur_token the current lookahead Symbol.
     */
    public void unrecovered_syntax_error(Symbol cur_token) throws Exception {
        report_fatal_error("Couldn't repair and continue parse", cur_token);
    }

    /**
     * Fetch an action from the action table.  The table is broken up into
     * rows, one per state (rows are indexed directly by state number).
     * Within each row, a list of index, value pairs are given (as sequential
     * entries in the table), and the list is terminated by a default entry
     * (denoted with a Symbol index of -1).  To find the proper entry in a row
     * we do a linear or binary search (depending on the size of the row).
     *
     * @param state the state index of the action being accessed.
     * @param sym   the Symbol index of the action being accessed.
     */
    protected final short get_action(int state, int sym) {
        short[] row = action_tab[state];

        /* linear search if we are < 10 entries */
        if (row.length < 20) {
            for (int probe = 0; probe < row.length; probe++) {
                /* is this entry labeled with our Symbol or the default? */
                short tag = row[probe++];
                if (tag == sym || tag == -1) {
                    /* return the next entry */
                    return row[probe];
                }
            }
            /* otherwise binary search */
        } else {

            int first = 0;
            int last = (row.length - 1) / 2 - 1; /* leave out trailing default entry */
            while (first <= last) {

                int probe = (first + last) / 2;
                if (sym == row[probe * 2]) {
                    return row[probe * 2 + 1];
                } else if (sym > row[probe * 2]) {
                    first = probe + 1;
                } else {
                    last = probe - 1;
                }
            }

            /* not found, use the default at the end */
            return row[row.length - 1];
        }

        /* shouldn't happened, but if we run off the end we return the default (error == 0) */
        return 0;
    }

    /**
     * Fetch a state from the reduce-goto table.  The table is broken up into
     * rows, one per state (rows are indexed directly by state number).
     * Within each row, a list of index, value pairs are given (as sequential
     * entries in the table), and the list is terminated by a default entry
     * (denoted with a Symbol index of -1).  To find the proper entry in a row
     * we do a linear search.
     *
     * @param state the state index of the entry being accessed.
     * @param sym   the Symbol index of the entry being accessed.
     */
    protected final short get_reduce(int state, int sym) {
        short[] row = reduce_tab[state];

        /* if we have a null row we go with the default */
        if (row == null) {
            return -1;
        }
        for (int probe = 0; probe < row.length; probe++) {
            /* is this entry labeled with our Symbol or the default? */
            short tag = row[probe++];
            if (tag == sym || tag == -1) {
                /* return the next entry */
                return row[probe];
            }
        }

        return -1;
    }

    /**
     * This method provides the main parsing routine.  It returns only when
     * done_parsing() has been called (typically because the parser has
     * accepted, or a fatal error has been reported).  See the header
     * documentation for the class regarding how shift/reduce parsers operate
     * and how the various tables are used.
     */
    public Symbol parse() throws Exception {
        /* the current action code */
        int act;

        /* the Symbol/stack element returned by a reduce */
        Symbol lhs_sym = null;

        /* information about production being reduced with */
        short handle_size, lhs_sym_num;

        /* set up direct reference to tables to drive the parser */
        production_tab = production_table();
        action_tab = action_table();
        reduce_tab = reduce_table();

        /* initialize the action encapsulation object */
        init_actions();

        /* do user initialization */
        user_init();

        /* get the first token */
        cur_token = scan();

        /* push dummy Symbol with start state to get us underway */
        stack.removeAllElements();
        stack.push(new Symbol(0, start_state()));
        tos = 0;

        /* continue until we are told to stop */
        for (_done_parsing = false; !_done_parsing; ) {
            /* Check current token for freshness. */
            if (cur_token.used_by_parser) {
                throw new Error("Symbol recycling detected (fix your scanner).");
            }
            /* current state is always on the top of the stack */

            /* look up action out of the current state with the current input */
            act = get_action(((Symbol) stack.peek()).parse_state, cur_token.sym);

            /* decode the action -- > 0 encodes shift */
            if (act > 0) {
                /* shift to the encoded state by pushing it on the stack */
                cur_token.parse_state = act - 1;
                cur_token.used_by_parser = true;
                stack.push(cur_token);
                tos++;

                /* advance to the next Symbol */
                cur_token = scan();
            }
            /* if its less than zero, then it encodes a reduce action */
            else if (act < 0) {
                /* perform the action for the reduce */
                lhs_sym = do_action(-act - 1, this, stack, tos);


                lhs_sym_num = production_tab[-act - 1][0];
                handle_size = production_tab[-act - 1][1];

                /* pop the handle off the stack */
                for (int i = 0; i < handle_size; i++) {
                    stack.pop();
                    tos--;
                }

                /* look up the state to go to from the one popped back to */
                act = get_reduce(((Symbol) stack.peek()).parse_state, lhs_sym_num);

                /* shift to that state */
                lhs_sym.parse_state = act;
                lhs_sym.used_by_parser = true;
                stack.push(lhs_sym);
                tos++;
            }
            /* finally if the entry is zero, we have an error */
            else if (act == 0) {
                /* call user syntax error reporting routine */
                syntax_error(cur_token);

                /* try to error recover */
                if (!error_recovery(false)) {
                    /* if that fails give up with a fatal syntax error */
                    unrecovered_syntax_error(cur_token);

                    /* just in case that wasn't fatal enough, end parse */
                    done_parsing();
                } else {
                    lhs_sym = (Symbol) stack.peek();
                }
            }
        }
        return lhs_sym;
    }

    /**
     * Write a debugging message to System.err for the debugging version
     * of the parser.
     *
     * @param mess the text of the debugging message.
     */
    public void debug_message(String mess) {
        System.err.println(mess);
    }

    /**
     * Dump the parse stack for debugging purposes.
     */
    public void dump_stack() {
        if (stack == null) {
            debug_message("# Stack dump requested, but stack is null");
            return;
        }
        debug_message("============ Parse Stack Dump ============");

        /* dump the stack */
        for (int i = 0; i < stack.size(); i++) {
            debug_message("Symbol: " + ((Symbol) stack.elementAt(i)).sym +
                    " State: " + ((Symbol) stack.elementAt(i)).parse_state);
        }
        debug_message("==========================================");
    }

    /**
     * Do debug output for a reduce.
     *
     * @param prod_num the production we are reducing with.
     * @param nt_num   the index of the LHS non terminal.
     * @param rhs_size the size of the RHS.
     */
    public void debug_reduce(int prod_num, int nt_num, int rhs_size) {
        debug_message("# Reduce with prod #" + prod_num + " [NT=" + nt_num +
                ", " + "SZ=" + rhs_size + "]");
    }

    /**
     * Do debug output for shift.
     *
     * @param shift_tkn the Symbol being shifted onto the stack.
     */
    public void debug_shift(Symbol shift_tkn) {
        debug_message("# Shift under term #" + shift_tkn.sym +
                " to state #" + shift_tkn.parse_state);
    }

    /**
     * Do debug output for stack state. [CSA]
     */
    public void debug_stack() {
        StringBuffer sb = new StringBuffer("## STACK:");
        for (int i = 0; i < stack.size(); i++) {
            Symbol s = (Symbol) stack.elementAt(i);
            sb.append(" <state " + s.parse_state + ", sym " + s.sym + ">");
            if ((i % 3) == 2 || (i == (stack.size() - 1))) {
                debug_message(sb.toString());
                sb = new StringBuffer("         ");
            }
        }
    }

    /**
     * Perform a parse with debugging output.  This does exactly the
     * same things as parse(), except that it calls debug_shift() and
     * debug_reduce() when shift and reduce moves are taken by the parser
     * and produces various other debugging messages.
     */
    public Symbol debug_parse()
            throws java.lang.Exception {
        /* the current action code */
        int act;

        /* the Symbol/stack element returned by a reduce */
        Symbol lhs_sym = null;

        /* information about production being reduced with */
        short handle_size, lhs_sym_num;

        /* set up direct reference to tables to drive the parser */
        production_tab = production_table();
        action_tab = action_table();
        reduce_tab = reduce_table();

        debug_message("# Initializing parser");

        /* initialize the action encapsulation object */
        init_actions();

        /* do user initialization */
        user_init();

        /* the current Symbol */
        cur_token = scan();

        debug_message("# Current Symbol is #" + cur_token.sym);

        /* push dummy Symbol with start state to get us underway */
        stack.removeAllElements();
        stack.push(new Symbol(0, start_state()));
        tos = 0;

        /* continue until we are told to stop */
        for (_done_parsing = false; !_done_parsing; ) {
            /* Check current token for freshness. */
            if (cur_token.used_by_parser)
                throw new Error("Symbol recycling detected (fix your scanner).");

            /* current state is always on the top of the stack */
            //debug_stack();

            /* look up action out of the current state with the current input */
            act = get_action(((Symbol) stack.peek()).parse_state, cur_token.sym);

            /* decode the action -- > 0 encodes shift */
            if (act > 0) {
                /* shift to the encoded state by pushing it on the stack */
                cur_token.parse_state = act - 1;
                cur_token.used_by_parser = true;
                debug_shift(cur_token);
                stack.push(cur_token);
                tos++;

                /* advance to the next Symbol */
                cur_token = scan();
                debug_message("# Current token is " + cur_token);
            }
            /* if its less than zero, then it encodes a reduce action */
            else if (act < 0) {
                /* perform the action for the reduce */
                lhs_sym = do_action((-act) - 1, this, stack, tos);

                /* look up information about the production */
                lhs_sym_num = production_tab[(-act) - 1][0];
                handle_size = production_tab[(-act) - 1][1];

                debug_reduce((-act) - 1, lhs_sym_num, handle_size);

                /* pop the handle off the stack */
                for (int i = 0; i < handle_size; i++) {
                    stack.pop();
                    tos--;
                }

                /* look up the state to go to from the one popped back to */
                act = get_reduce(((Symbol) stack.peek()).parse_state, lhs_sym_num);
                debug_message("# Reduce rule: top state " +
                        ((Symbol) stack.peek()).parse_state +
                        ", lhs sym " + lhs_sym_num + " -> state " + act);

                /* shift to that state */
                lhs_sym.parse_state = act;
                lhs_sym.used_by_parser = true;
                stack.push(lhs_sym);
                tos++;

                debug_message("# Goto state #" + act);
            }
            /* finally if the entry is zero, we have an error */
            else if (act == 0) {
                /* call user syntax error reporting routine */
                syntax_error(cur_token);

                /* try to error recover */
                if (!error_recovery(true)) {
                    /* if that fails give up with a fatal syntax error */
                    unrecovered_syntax_error(cur_token);

                    /* just in case that wasn't fatal enough, end parse */
                    done_parsing();
                } else {
                    lhs_sym = (Symbol) stack.peek();
                }
            }
        }
        return lhs_sym;
    }


    /**
     * Attempt to recover from a syntax error.  This returns false if recovery
     * fails, true if it succeeds.  Recovery happens in 4 steps.  First we
     * pop the parse stack down to a point at which we have a shift out
     * of the top-most state on the error Symbol.  This represents the
     * initial error recovery configuration.  If no such configuration is
     * found, then we fail.  Next a small number of "lookahead" or "parse
     * ahead" Symbols are read into a buffer.  The size of this buffer is
     * determined by error_sync_size() and determines how many Symbols beyond
     * the error must be matched to consider the recovery a success.  Next,
     * we begin to discard Symbols in attempt to get past the point of error
     * to a point where we can continue parsing.  After each Symbol, we attempt
     * to "parse ahead" though the buffered lookahead Symbols.  The "parse ahead"
     * process simulates that actual parse, but does not modify the real
     * parser's configuration, nor execute any actions. If we can  parse all
     * the stored Symbols without error, then the recovery is considered a
     * success.  Once a successful recovery point is determined, we do an
     * actual parse over the stored input -- modifying the real parse
     * configuration and executing all actions.  Finally, we return the the
     * normal parser to continue with the overall parse.
     *
     * @param debug should we produce debugging messages as we parse.
     */
    protected boolean error_recovery(boolean debug)
            throws Exception {
        if (debug) debug_message("# Attempting error recovery");

      /* first pop the stack back into a state that can shift on error and
	 do that shift (if that fails, we fail) */
        if (!find_recovery_config(debug)) {
            if (debug) debug_message("# Error recovery fails");
            return false;
        }

        /* read ahead to create lookahead we can parse multiple times */
        read_lookahead();

        /* repeatedly try to parse forward until we make it the required dist */
        for (; ; ) {
            /* try to parse forward, if it makes it, bail out of loop */
            if (debug) debug_message("# Trying to parse ahead");
            if (try_parse_ahead(debug)) {
                break;
            }

            /* if we are now at EOF, we have failed */
            if (lookahead[0].sym == EOF_sym()) {
                if (debug) debug_message("# Error recovery fails at EOF");
                return false;
            }

            /* otherwise, we consume another Symbol and try again */
            // BUG FIX by Bruce Hutton
            // Computer Science Department, University of Auckland,
            // Auckland, New Zealand.
            // It is the first token that is being consumed, not the one
            // we were up to parsing
            if (debug)
                debug_message("# Consuming Symbol #" + lookahead[0].sym);
            restart_lookahead();
        }

        /* we have consumed to a point where we can parse forward */
        if (debug) debug_message("# Parse-ahead ok, going back to normal parse");

        /* do the real parse (including actions) across the lookahead */
        parse_lookahead(debug);

        /* we have success */
        return true;
    }

    /**
     * Determine if we can shift under the special error Symbol out of the
     * state currently on the top of the (real) parse stack.
     */
    protected boolean shift_under_error() {
        /* is there a shift under error Symbol */
        return get_action(((Symbol) stack.peek()).parse_state, error_sym()) > 0;
    }

    /**
     * Put the (real) parse stack into error recovery configuration by
     * popping the stack down to a state that can shift on the special
     * error Symbol, then doing the shift.  If no suitable state exists on
     * the stack we return false
     *
     * @param debug should we produce debugging messages as we parse.
     */
    protected boolean find_recovery_config(boolean debug) {
        Symbol error_token;
        int act;

        if (debug) debug_message("# Finding recovery state on stack");

        /* Remember the right-position of the top symbol on the stack */
        int right_pos = ((Symbol) stack.peek()).right;
        int left_pos = ((Symbol) stack.peek()).left;

        /* pop down until we can shift under error Symbol */
        while (!shift_under_error()) {
            /* pop the stack */
            if (debug)
                debug_message("# Pop stack by one, state was # " +
                        ((Symbol) stack.peek()).parse_state);
            left_pos = ((Symbol) stack.pop()).left;
            tos--;

            /* if we have hit bottom, we fail */
            if (stack.empty()) {
                if (debug) debug_message("# No recovery state found on stack");
                return false;
            }
        }

        /* state on top of the stack can shift under error, find the shift */
        act = get_action(((Symbol) stack.peek()).parse_state, error_sym());
        if (debug) {
            debug_message("# Recover state found (#" +
                    ((Symbol) stack.peek()).parse_state + ")");
            debug_message("# Shifting on error to state #" + (act - 1));
        }

        /* build and shift a special error Symbol */
        error_token = new Symbol(error_sym(), left_pos, right_pos);
        error_token.parse_state = act - 1;
        error_token.used_by_parser = true;
        stack.push(error_token);
        tos++;

        return true;
    }


    /**
     * Lookahead Symbols used for attempting error recovery "parse aheads".
     */
    protected Symbol lookahead[];

    /**
     * Position in lookahead input buffer used for "parse ahead".
     */
    protected int lookahead_pos;

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /**
     * Read from input to establish our buffer of "parse ahead" lookahead
     * Symbols.
     */
    protected void read_lookahead() throws java.lang.Exception {
        /* create the lookahead array */
        lookahead = new Symbol[error_sync_size()];

        /* fill in the array */
        for (int i = 0; i < error_sync_size(); i++) {
            lookahead[i] = cur_token;
            cur_token = scan();
        }

        /* start at the beginning */
        lookahead_pos = 0;
    }

    /**
     * Return the current lookahead in our error "parse ahead" buffer.
     */
    protected Symbol cur_err_token() {
        return lookahead[lookahead_pos];
    }

    /*. . . . . . . . . . . . . . . . . . . . . . . . . . . . . .*/

    /**
     * Advance to next "parse ahead" input Symbol. Return true if we have
     * input to advance to, false otherwise.
     */
    protected boolean advance_lookahead() {
        /* advance the input location */
        lookahead_pos++;

        /* return true if we didn't go off the end */
        return lookahead_pos < error_sync_size();
    }

    /**
     * Reset the parse ahead input to one Symbol past where we started error
     * recovery (this consumes one new Symbol from the real input).
     */
    protected void restart_lookahead() throws java.lang.Exception {
        /* move all the existing input over */
        for (int i = 1; i < error_sync_size(); i++)
            lookahead[i - 1] = lookahead[i];

        /* read a new Symbol into the last spot */
        // BUG Fix by Bruce Hutton
        // Computer Science Department, University of Auckland,
        // Auckland, New Zealand. [applied 5-sep-1999 by csa]
        // The following two lines were out of order!!
        lookahead[error_sync_size() - 1] = cur_token;
        cur_token = scan();

        /* reset our internal position marker */
        lookahead_pos = 0;
    }

    /**
     * Do a simulated parse forward (a "parse ahead") from the current
     * stack configuration using stored lookahead input and a virtual parse
     * stack.  Return true if we make it all the way through the stored
     * lookahead input without error. This basically simulates the action of
     * parse() using only our saved "parse ahead" input, and not executing any
     * actions.
     *
     * @param debug should we produce debugging messages as we parse.
     */
    protected boolean try_parse_ahead(boolean debug)
            throws java.lang.Exception {
        int act;
        short lhs, rhs_size;

        /* create a virtual stack from the real parse stack */
        virtual_parse_stack vstack = new virtual_parse_stack(stack);

        /* parse until we fail or get past the lookahead input */
        for (; ; ) {
            /* look up the action from the current state (on top of stack) */
            act = get_action(vstack.top(), cur_err_token().sym);

            /* if its an error, we fail */
            if (act == 0) return false;

            /* > 0 encodes a shift */
            if (act > 0) {
                /* push the new state on the stack */
                vstack.push(act - 1);

                if (debug) debug_message("# Parse-ahead shifts Symbol #" +
                        cur_err_token().sym + " into state #" + (act - 1));

                /* advance simulated input, if we run off the end, we are done */
                if (!advance_lookahead()) return true;
            }
            /* < 0 encodes a reduce */
            else {
                /* if this is a reduce with the start production we are done */
                if ((-act) - 1 == start_production()) {
                    if (debug) debug_message("# Parse-ahead accepts");
                    return true;
                }

                /* get the lhs Symbol and the rhs size */
                lhs = production_tab[(-act) - 1][0];
                rhs_size = production_tab[(-act) - 1][1];

                /* pop handle off the stack */
                for (int i = 0; i < rhs_size; i++)
                    vstack.pop();

                if (debug)
                    debug_message("# Parse-ahead reduces: handle size = " +
                            rhs_size + " lhs = #" + lhs + " from state #" + vstack.top());

                /* look up goto and push it onto the stack */
                vstack.push(get_reduce(vstack.top(), lhs));
                if (debug)
                    debug_message("# Goto state #" + vstack.top());
            }
        }
    }

    /**
     * Parse forward using stored lookahead Symbols.  In this case we have
     * already verified that parsing will make it through the stored lookahead
     * Symbols and we are now getting back to the point at which we can hand
     * control back to the normal parser.  Consequently, this version of the
     * parser performs all actions and modifies the real parse configuration.
     * This returns once we have consumed all the stored input or we accept.
     *
     * @param debug should we produce debugging messages as we parse.
     */
    protected void parse_lookahead(boolean debug)
            throws java.lang.Exception {
        /* the current action code */
        int act;

        /* the Symbol/stack element returned by a reduce */
        Symbol lhs_sym = null;

        /* information about production being reduced with */
        short handle_size, lhs_sym_num;

        /* restart the saved input at the beginning */
        lookahead_pos = 0;

        if (debug) {
            debug_message("# Reparsing saved input with actions");
            debug_message("# Current Symbol is #" + cur_err_token().sym);
            debug_message("# Current state is #" +
                    ((Symbol) stack.peek()).parse_state);
        }

        /* continue until we accept or have read all lookahead input */
        while (!_done_parsing) {
            /* current state is always on the top of the stack */

            /* look up action out of the current state with the current input */
            act =
                    get_action(((Symbol) stack.peek()).parse_state, cur_err_token().sym);

            /* decode the action -- > 0 encodes shift */
            if (act > 0) {
                /* shift to the encoded state by pushing it on the stack */
                cur_err_token().parse_state = act - 1;
                cur_err_token().used_by_parser = true;
                if (debug) debug_shift(cur_err_token());
                stack.push(cur_err_token());
                tos++;

                /* advance to the next Symbol, if there is none, we are done */
                if (!advance_lookahead()) {
                    if (debug) debug_message("# Completed reparse");

                    /* scan next Symbol so we can continue parse */
                    // BUGFIX by Chris Harris <ckharris@ucsd.edu>:
                    //   correct a one-off error by commenting out
                    //   this next line.
                    /*cur_token = scan();*/

                    /* go back to normal parser */
                    return;
                }

                if (debug)
                    debug_message("# Current Symbol is #" + cur_err_token().sym);
            }
            /* if its less than zero, then it encodes a reduce action */
            else if (act < 0) {
                /* perform the action for the reduce */
                lhs_sym = do_action((-act) - 1, this, stack, tos);

                /* look up information about the production */
                lhs_sym_num = production_tab[(-act) - 1][0];
                handle_size = production_tab[(-act) - 1][1];

                if (debug) debug_reduce((-act) - 1, lhs_sym_num, handle_size);

                /* pop the handle off the stack */
                for (int i = 0; i < handle_size; i++) {
                    stack.pop();
                    tos--;
                }

                /* look up the state to go to from the one popped back to */
                act = get_reduce(((Symbol) stack.peek()).parse_state, lhs_sym_num);

                /* shift to that state */
                lhs_sym.parse_state = act;
                lhs_sym.used_by_parser = true;
                stack.push(lhs_sym);
                tos++;

                if (debug) debug_message("# Goto state #" + act);

            }
	  /* finally if the entry is zero, we have an error
	     (shouldn't happen here, but...)*/
            else if (act == 0) {
                report_fatal_error("Syntax error", lhs_sym);
                return;
            }
        }


    }

    /**
     * Utility function: unpacks parse tables from strings
     */
    protected static short[][] unpackFromStrings(String[] sa) {
        // Concatanate initialization strings.
        StringBuffer sb = new StringBuffer(sa[0]);
        for (int i = 1; i < sa.length; i++)
            sb.append(sa[i]);
        int n = 0; // location in initialization string
        int size1 = (((int) sb.charAt(n)) << 16) | ((int) sb.charAt(n + 1));
        n += 2;
        short[][] result = new short[size1][];
        for (int i = 0; i < size1; i++) {
            int size2 = (((int) sb.charAt(n)) << 16) | ((int) sb.charAt(n + 1));
            n += 2;
            result[i] = new short[size2];
            for (int j = 0; j < size2; j++)
                result[i][j] = (short) (sb.charAt(n++) - 2);
        }
        return result;
    }

}
