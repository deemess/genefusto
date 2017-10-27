/*
Here's what I know about the pinouts, though some of the higher address
bits are really guesses, since I couldn't fiddle with all of them
(but they seem to be less mixed up than the opcodes).
This is some C code to change between the codes and 68000 hex.
*/

/*
 * "Eet ees not *I* who am crazy, eet ees I who am MAD!" - R. Ho"ek
 *
 * lamp.c - a program to control Game Genie for Genesis
 *
 * Translate between Game Genie for Genesis 8-character codes and
 * the 24-bit address & 16-bit value for that address.
 * The format is this:
 *
 * ijklm nopIJ KLMNO PABCD EFGHd efgha bcQRS TUVWX   decodes to...
 *
 * ABCDEFGH IJKLMNOP QRSTUVWX: abcdefgh ijklmnop
 * 24-bit address              16-bit data
 * MSB                    LSB  MSB           LSB
 *
 * ..where each group of five letters is a Genie code character
 * (ABCDEFGHJKLMNPRSTVWXYZ0123456789, each representing 5 bits 00000-11111),
 * and each 8-character Genie code is the 24-bit address and 16-bit data.
 *
 * For example, SCRA-BJX0 is a game genie code.  Each letter is 5 bits from
 * the table ABCDEFGHJKLMNPRSTVWXYZ0123456789, A=00000, B=00001, C=00010...
 *
 *   S     C     R     A  -  B     J     X     0
 * 01111 00010 01110 00000 00001 01000 10011 10110
 * ijklm nopIJ KLMNO PABCD EFGHd efgha bcQRS TUVWX   rearrange as...
 *
 * 00000000 10011100 01110110: 01010100 01111000
 * ABCDEFGH IJKLMNOP QRSTUVWX: abcdefgh ijklmnop
 * 24-bit address              16-bit data
 * MSB                    LSB  MSB           LSB
 *
 * Which is 009c76: 5478
 *
 * See the usage message when run with no arguments...
 * Merlyn LeRoy (Brian Westley), merlyn@digibd.com 1/4/93
 */

#include <stdio.h>

int argc, data;
char **argv, *chars = (char *)0;
char gg[] =
"AaBbCcDdEeFfGgHhJjKkLlMmNnPpRrSsTtVvWwXxYyZz0O1I223344556677889";

int getachar()
{
    if (argc > 1) {
        if (!chars) {
            data = 1;
            chars = argv[data];
        }
        if (!*chars) {
            data++;
            if (data >= argc)
                return (EOF);
            chars = argv[data];
            return (' ');
        }
        return (*chars++);
    }
    else {
        return (getchar());
    }
}

instr2gen(addr, instr)
unsigned long addr, instr;
{
    int i;
    unsigned long bits[8];

    bits[0] =  (instr >>  3) & 0x1f;
    bits[1] = ((instr <<  2) & 0x1c) | ((addr  >> 14) & 0x03);
    bits[2] =  (addr  >>  9) & 0x1f;
    bits[3] = ((addr  >>  4) & 0x10) | ((addr  >> 20) & 0x0f);
    bits[4] = ((addr  >> 15) & 0x1e) | ((instr >> 12) & 0x01);
    bits[5] = ((instr >>  7) & 0x1e) | ((instr >> 15) & 0x01);
    bits[6] = ((instr >> 10) & 0x18) | ((addr  >>  5) & 0x07);
    bits[7] =   addr         & 0x1f;

    for (i = 0; i < 8; i++) {
        putchar(gg[bits[i]<<1]);
        if (i == 3)
            putchar('-');
    }

    /* print lowest 8-bits as decimal value if it's a moveq instr or */
    /* if the top 8 bits are zero (probably a wordsize move immediate) */
    if ((instr & 0xf100L) == 0x7000L || (instr & 0xff00L) == 0x0000L) {
        printf(" [%3ld]", instr & 0xff);
    }
    else {
	/* print +1..8 or -1..8 for addq & subq instr */
        if ((instr & 0xf000L) == 0x5000L && (instr & 0x00c0L) != 0x00c0L) {
	    printf(" %c%ld   ", ((instr & 0x0100L) ? '-' : '+'),
		((instr & 0x0e00L) ? ((instr>>9) & 0x07) : 8));
        }
        else
            printf("      ");
    }
}

hex2gen(line)
unsigned char *line;
{
    unsigned char *old_line, *hp;
    static unsigned long addr;
    unsigned long instr;
    static char hex[] = "00112233445566778899AaBbCcDdEeFf";

    do {
        old_line = line;
        instr = 0;

        while (*line && (*line == ':' || *line == ' '))
            line++;
        if (!*line)
            return;

        while (*line && *line != ' ' && *line != ':') {
	    hp = hex;
	    while (*hp && *hp != *line)
		hp++;
	    if (*hp) {
		instr = 0x10L * instr + ((hp - hex)>>1);
	    }
            else {
                fprintf(stderr, "Bad hex code '%s'\n", old_line);
		instr = 0;
		while (*line && *line != ' ' && *line != ':')
		    line++;
		break;
            }
            line++;
        }
        if (*line == ':') {
	    addr = instr;
	    if (addr & 0xff000000L) {
		fprintf(stderr, "Warning: address too large\n");
	    }
            if (addr & 0x00000001L)
                fprintf(stderr, "Warning: odd address\n");
            addr -= 2;
            continue;
        }
        else {
	    if (instr & 0xffff0000L) {
		fprintf(stderr, "Warning: instruction too large\n");
		instr &= 0x0000ffffL;
	    }
            addr += 2;
        }

        while (*line && *line == ' ')
            line++;

        instr2gen(addr, instr);
        printf(" = %06lx: %04lx\n", addr & 0x00ffffffL, instr);
    } while (*line);
}

gen2hex(line)
unsigned char *line;
{
    int i;
    unsigned char *g, *old_line;
    unsigned long bits[8];
    unsigned long addr, instr;

    old_line = line;
    for (i = 0; i < 8; i++) {
        while (*line) {
            g = gg;
            while (*g && *g != *line)
                g++;
            if (*g == *line)
                break;
            line++;
        }
        if (*line)
            bits[i] = (g - gg)>>1;
        else {
            fprintf(stderr, "Bad Genie code '%s'\n", old_line);
            return;
        }
        line++;
    }

    addr =
        ((bits[3] & 0x0f) << 20) |
        ((bits[4] & 0x1e) << 15) |
        ((bits[1] & 0x03) << 14) |
        ((bits[2] & 0x1f) <<  9) |
        ((bits[3] & 0x10) <<  4) |
        ((bits[6] & 0x07) <<  5) |
        ((bits[7] & 0x1f) <<  0);
    instr =
        ((bits[5] & 0x01) << 15) |
        ((bits[6] & 0x18) << 10) |
        ((bits[4] & 0x01) << 12) |
        ((bits[5] & 0x1e) <<  7) |
        ((bits[0] & 0x1f) <<  3) |
        ((bits[1] & 0x1c) >>  2);

    while (*line && *line == ' ')
        line++;
    if (*line == '[') {
        line++;
        i = 0;
	while ('0' <= *line && *line <= '9') {
            i = i * 10 + *line - '0';
            line++;
        }
        if (*line == ']')
            line++;
        instr = (instr & 0xff00L) | (i & 0xff);
    }
    else if (*line == '+' || *line == '-') {
        if (*line == '-') {
            instr |= 0x0100L;
        }
        else
            instr &= ~0x0100L;
        line++;
        i = *line - '0';
        instr = (instr & 0xf1ffL) | ((i<<9) & 0x0e00L);
    }

    if (addr & 0x00000001L)
        fprintf(stderr, "Warning: odd address\n");
    instr2gen(addr, instr);
    printf(" = %06lx: %04lx\n", addr, instr);
}

main(ac, av)
int ac;
unsigned char **av;
{
    int ch, i, flag;
    unsigned char line[128];

    argc = ac;
    argv = av;

    if (argc == 1) {
puts("This program will translate between Game Genie for Genesis codes");
puts("and the 24-bit address and 16-bit data that the code represents.");
puts("Enter the Game Genie code(s) as usual, e.g. SCRA-BJX0 and you will");
puts("get the address and 16-bit data as 009c76: 5478");
puts("You may then disassemble the 16-bit data as a 68000 instruction.\n");
puts("This also works in reverse; you can enter the address & data and");
puts("get the 8-letter Genie code.  In addition, entering a number in");
puts("square brackets after a genie code will change the lower 8 bits");
puts("to match (for number of starting lives, for example).");
puts("Entering a number as +N or -N (N=1..8) after a genie code will");
puts("change the various increment/decrement instructions.\n");

    }

    do {
        i = 0;
        flag = 0;
        while ((ch=getachar()) != EOF && ch != '\n' && i < sizeof(line)-1) {
            if (ch == '-')
                flag++;
            line[i++] = ch;
        }
        line[i] = 0;

        if (i) {
        	if (flag)
                gen2hex(line);
            else
                hex2gen(line);
        }
    } while (ch != EOF);
}

