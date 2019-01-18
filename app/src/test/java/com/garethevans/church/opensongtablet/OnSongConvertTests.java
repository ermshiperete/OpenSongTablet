package com.garethevans.church.opensongtablet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class OnSongConvertTests {

    @Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                { /*input:*/ " Intro:",       /*expected:*/ "[Intro]", "#01"},
                { /*input:*/ " Outro:",       /*expected:*/ "[Outro]", "#02"},
                { /*input:*/ " V:",           /*expected:*/ "[V]",     "#03"},
                { /*input:*/ " V1:",          /*expected:*/ "[V1]",    "#04"},
                { /*input:*/ " V2:",          /*expected:*/ "[V2]",    "#05"},
                { /*input:*/ " V9:",          /*expected:*/ "[V9]",    "#06"},
                { /*input:*/ " Verse:",       /*expected:*/ "[V]",     "#07"},
                { /*input:*/ " Verse 1:",     /*expected:*/ "[V1]",    "#08"},
                { /*input:*/ " Verse 2:",     /*expected:*/ "[V2]",    "#09"},
                { /*input:*/ " Verse 3:",     /*expected:*/ "[V3]",    "#10"},
                { /*input:*/ " Verse 4:",     /*expected:*/ "[V4]",    "#11"},
                { /*input:*/ " (Verse)",      /*expected:*/ "[V]",     "#12"},
                { /*input:*/ " (Verse 1)",    /*expected:*/ "[V1]",    "#13"},
                { /*input:*/ " (Verse 2)",    /*expected:*/ "[V2]",    "#14"},
                { /*input:*/ " (Verse 3)",    /*expected:*/ "[V3]",    "#15"},
                { /*input:*/ " (Chorus)",     /*expected:*/ "[C]",     "#16"},
                { /*input:*/ " Chorus",       /*expected:*/ "[C]",     "#17"},
                { /*input:*/ " C:",           /*expected:*/ "[C]",     "#18"},
                { /*input:*/ " C1:",          /*expected:*/ "[C1]",    "#19"},
                { /*input:*/ " C2:",          /*expected:*/ "[C2]",    "#20"},
                { /*input:*/ " C3:",          /*expected:*/ "[C3]",    "#21"},
                { /*input:*/ " C4:",          /*expected:*/ "[C4]",    "#22"},
                { /*input:*/ " C9:",          /*expected:*/ "[C9]",    "#23"},
                { /*input:*/ " Chorus:",      /*expected:*/ "[C]",     "#24"},
                { /*input:*/ " Chorus 1:",    /*expected:*/ "[C1]",    "#25"},
                { /*input:*/ " Chorus 2:",    /*expected:*/ "[C2]",    "#26"},
                { /*input:*/ " Chorus 3:",    /*expected:*/ "[C3]",    "#27"},
                { /*input:*/ " Prechorus:",   /*expected:*/ "[P]",     "#28"},
                { /*input:*/ " Prechorus 1:", /*expected:*/ "[P1]",    "#29"},
                { /*input:*/ " Prechorus 2:", /*expected:*/ "[P2]",    "#30"},
                { /*input:*/ " Prechorus 3:", /*expected:*/ "[P3]",    "#31"},
                { /*input:*/ " Bridge:",      /*expected:*/ "[B]",     "#32"},
                { /*input:*/ " Tag:",         /*expected:*/ "[T]",     "#33"},
                { /*input:*/ "Intro:",        /*expected:*/ "[Intro]", "#34"},
                { /*input:*/ "Outro:",        /*expected:*/ "[Outro]", "#35"},
                { /*input:*/ "V:",            /*expected:*/ "[V]",     "#36"},
                { /*input:*/ "V1:",           /*expected:*/ "[V1]",    "#37"},
                { /*input:*/ "V2:",           /*expected:*/ "[V2]",    "#38"},
                { /*input:*/ "V8:",           /*expected:*/ "[V8]",    "#39"},
                { /*input:*/ "V9:",           /*expected:*/ "[V9]",    "#40"},
                { /*input:*/ "Verse:",        /*expected:*/ "[V]",     "#41"},
                { /*input:*/ "Verse 1:",      /*expected:*/ "[V1]",    "#42"},
                { /*input:*/ "Verse 4:",      /*expected:*/ "[V4]",    "#43"},
                { /*input:*/ "(Verse)",       /*expected:*/ "[V]",     "#44"},
                { /*input:*/ "(Verse 3)",     /*expected:*/ "[V3]",    "#45"},
                { /*input:*/ "(Chorus)",      /*expected:*/ "[C]",     "#46"},
                { /*input:*/ "C:",            /*expected:*/ "[C]",     "#47"},
                { /*input:*/ "C1:",           /*expected:*/ "[C1]",    "#48"},
                { /*input:*/ "C2:",           /*expected:*/ "[C2]",    "#49"},
                { /*input:*/ "C9:",           /*expected:*/ "[C9]",    "#50"},
                { /*input:*/ "Chorus:",       /*expected:*/ "[C]",     "#51"},
                { /*input:*/ "Chorus 1:",     /*expected:*/ "[C1]",    "#52"},
                { /*input:*/ "Chorus 3:",     /*expected:*/ "[C3]",    "#53"},
                { /*input:*/ "Prechorus:",    /*expected:*/ "[P]",     "#54"},
                { /*input:*/ "Prechorus 1:",  /*expected:*/ "[P1]",    "#55"},
                { /*input:*/ "Bridge:",       /*expected:*/ "[B]",     "#56"},
                { /*input:*/ "Tag:",          /*expected:*/ "[T]",     "#57"},
                { /*input:*/ "Vers 1",        /*expected:*/ "[V1]",    "#58"},
                { /*input:*/ "Refrain",       /*expected:*/ "[C]",     "#59"},
                { /*input:*/ "Schluss",       /*expected:*/ "[Schluss]","#60"},
        });
    }

    @Parameter // first data value (0) is default
    public String input;

    @Parameter(1)
    public String expected;

    @Parameter(2)
    public String message;

    @Test
    public void guessTags_DetectsTags() {
        assertEquals(message + " Input: '" + input + "'", expected, OnSongConvert.guessTags(input));
    }
}
