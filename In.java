import java.io.*;

/** Simple input from the keyboard or from a file.
<p>This class allows reading formatted data either from the keyboard
or from a file. It is intended to be used in an introductory
programming course when classes, packages and exceptions are unknown
at the beginning. To use it, simply copy In.class into the
source file directory. </p>

<p>All input comes from the current input file, which is initially
the keyboard. Opening a file with open() makes it the new current
input file. Closing a file with close() switches back to the previous
input file.</p>

<p>When reading from the keyboard, reading blocks until the user has entered
a sequence of characters terminated by the return key. All methods read
from this input buffer (including the terminating '\r' and '\n') until the
buffer is fully consumed. When a method tries to read beyond the end
of the buffer, it blocks again waiting for the next buffer.</p>

<p>End of file detection: When reading from the keyboard, eof can be
signaled as ctrl-Z at the beginning of a new line. When reading from a file,
eof occurs when an attempt is made to read beyond the end of the file.
In either case In.done() returns false if the requested data could not
be read because of eof. </p>
*/
public class In {

/** End of file indicator returned by read() or peek() when no more
characters can be read.
*/
public  static final char eof   = '\uffff';

private static final int empty = '\ufffe';
private static final int Windows = 0, MacOS = 1, Unix = 2;

private static final char eofChar = '\u0005';  // ctrl E
private static InputStream in;
private static InputStream[] stack;
private static int sp;
private static boolean done; // true if recent operation was successful
private static char buf;     // last read character
private static char eol;     // the eol character of the respective OS
private static int OS;       // current operating system

private static char charAfterWhiteSpace() {
	char c;
	do c = read(); while (done && c <= ' ');
	return c;
}

private static String readDigits() {
	StringBuffer b = new StringBuffer();
	char c = charAfterWhiteSpace();
	if (done && c == '-') {
		b.append(c);
		c = read();
	}
	while (done && Character.isDigit(c)) {
		b.append(c);
		c = read();
	}
	buf = c;
	return b.toString();
}

private static String readFloatDigits() {
	StringBuffer b = new StringBuffer();
	char c = charAfterWhiteSpace();
	if (done && (c == '+' || c == '-')) {
		b.append(c);
		c = read();
	}
	while (done && Character.isDigit(c)) {
		b.append(c);
		c = read();
	}
	if (done && (c == '.')) {
		b.append(c);
		c = read();
		while (done && Character.isDigit(c)) {
			b.append(c);
			c = read();
		}
	}
	if (done && (c == 'e' || c == 'E')) {
		b.append(c);
		c = read();
		if (done && (c == '+' || c == '-')) {
			b.append(c);
			c = read();
		}
		while (done && Character.isDigit(c)) {
			b.append(c);
			c = read();
		}
	}
	buf = c;
	return b.toString();
}


/** Read a character (byte).
If an attempt is made to read beyond the end of the file,
eof is returned and done() yields false. Otherwise the read byte
is in the range 0..255.
*/
public static char read() {
	char c;
	if (buf != empty) {
		c = buf;
		if (buf != eof) buf = empty;
	} else {
		try {
			c = (char)in.read();
		} catch (IOException e) {
			done = false;
			c = eof; buf = eof;
		}
	}
	if (sp == 0 && c == eofChar) { c = eof; buf = eof; }
	done = c != eof;
	return c;
}

/** Read a boolean value.
This method skips white space and tries to read an identifier. If its value
is "true" the method returns true otherwise false. If the identifier is neither
"true" nor "false" done() yields false.
*/
public static boolean readBoolean() {
	String s = readIdentifier();
	done = true;
	if (s.equals("true")) return true;
	else { done = s.equals("false"); return false; }
}

/** Read an identifier.
This method skips white space and tries to read an identifier starting
with a letter and continuing with letters or digits. If a token of this
structure could be read, it is returned otherwise the empty string is
returned and done() yields false.
*/
public static String readIdentifier() {
	StringBuffer b = new StringBuffer();
	char c = charAfterWhiteSpace();
	if (done && Character.isLetter(c)) {
		b.append(c);
		c = read();
		while (done && (Character.isLetter(c) || Character.isDigit(c))) {
			b.append(c);
			c = read();
		}
	}
	buf = c;
	done = b.length() > 0;
	return b.toString();
}

/** Read a word.
This method skips white space and tries to read a word consisting of
all characters up to the next white space or to the end of the file.
If a token of this structure could be read, it is returned otherwise
an empty string is returned and done() yields false.
*/
public static String readWord() {
	StringBuffer b = new StringBuffer();
	char c = charAfterWhiteSpace();
	while (done && c > ' ') {
		b.append(c);
		c = read();
	}
	buf = c;
	done = b.length() > 0;
	return b.toString();
}

/** Read a line of text.
This method reads the rest of the current line (including eol) and
returns it (excluding eol). A line may be empty.
*/
public static String readLine() {
	StringBuffer b = new StringBuffer();
	char c = read();
	while (done && c != eol) {
		b.append(c);
		c = read();
	}
	if (OS == Windows && c == eol) c = read(); // read also LF
	buf = empty;
	if (b.length() > 0) done = true;
	return b.toString();
}

/** Read the whole file.
This method reads from the current position to the end of the
file and returns its text in a single large string. done() yields
always true.
*/
public static String readFile() {
	StringBuffer b = new StringBuffer();
	char c = charAfterWhiteSpace();
	while (done) {
		b.append(c);
		c = read();
	}
	buf = eof;
	done = true;
	return b.toString();
}

/** Read a quote-delimited string.
This method skips white space and tries to read a string in the form "...".
It can be used to read pieces of text that contain white space.
*/
public static String readString() {
	StringBuffer b = new StringBuffer();
	char c = charAfterWhiteSpace();
	if (done && c == '"') {
		c = read();
		while (done && c != '"') {
			b.append(c);
			c = read();
		}
		if (c == '"') { c = read(); done = true; } else done = false;
	} else done = false;
	buf = c;
	return b.toString();
}

/** Read an integer.
This method skips white space and tries to read an integer. If the
text does not contain an integer or if the number is too big, the
value 0 is returned and the subsequent call of done() yields false.
An integer is a sequence of digits, possibly preceded by '-'.
*/
public static int readInt() {
	String s = readDigits();
	try {
		done = true;
		return Integer.parseInt(s);
	} catch (Exception e) {
		done = false; return 0;
	}
}

/** Read a long integer.
This method skips white space and tries to read a long integer. If the
text does not contain a number or if the number is too big, the
value 0 is returned and the subsequent call of done() yields false.
A long integer is a sequence of digits, possibly preceded by '-'.
*/
public static long readLong() {
	String s = readDigits();
	try {
		done = true;
		return Long.parseLong(s);
	} catch (Exception e) {
		done = false; return 0;
	}
}

/** Read a float value.
This method skips white space and tries to read a float value. If the
text does not contain a float value or if the number is not well-formed,
the value 0f is returned and the subsequent call of done() yields false.
An float value is as specified in the Java language description. It may
be preceded by a '+' or a '-'.
*/
public static float readFloat() {
	String s = readFloatDigits();
	try {
		done = true;
		return Float.parseFloat(s);
	} catch (Exception e) {
		done = false; return 0f;
	}
}

/** Read a double value.
This method skips white space and tries to read a double value. If the
text does not contain a double value or if the number is not well-formed,
the value 0.0 is returned and the subsequent call of done() yields false.
An double value is as specified in the Java language description. It may
be preceded by a '+' or a '-'.
*/
public static double readDouble() {
	String s = readFloatDigits();
	try {
		done = true;
		return Double.parseDouble(s);
	} catch (Exception e) {
		done = false; return 0.0;
	}
}

/** Peek at the next character.
This method skips white space and returns the next character without removing
it from the input stream. It can be used to find out, what token comes nest
in the input stream.
*/
public static char peek() {
	char c = charAfterWhiteSpace();
	buf = c;
	return c;
}

/** Open a text file for reading
The text file with the name fn is opened as the new current input
file. When it is closed again, the previous input file is restored.
*/
public static void open(String fn) {
	try {
		InputStream s = new FileInputStream(fn);
		stack[sp++] = in;
		in = s;
		done = true;
	} catch (FileNotFoundException e) {
		done = false;
	}
	buf = empty;
}

/** Close the current input file.
The current input file is closed and the previous input file is
restored. Closing the keyboard input has no effect but causes
done() to yield false.
*/
public static void close() {
	try {
		if (sp > 0) {
			in.close();
			in = stack[--sp];
			done = true;
		} else {
			done = false;
		}
	} catch (IOException e) {
		done = false;
	}
	buf = empty;
}

/** Check if the previous operation was successful.
This method returns true if the previous read operation was able
to read a token of the requested structure. It can also be called
after open() and close() to check if these operations were successful.
If done() is called before any other operation it yields true.
*/
public static boolean done() {
	return done;
}

static { // initializer
	done = true;
	in = System.in;
	stack = new InputStream[8];
	sp = 0;
	buf = empty;
//        OS = Windows;
//        OS = MacOS;
        OS = Unix;
	if (OS == Windows) eol = (char)13; else eol = (char)10;
}


}
