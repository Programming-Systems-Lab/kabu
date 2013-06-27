// This is a mutant program.
// Author : ysma

package edu.columbia.cs.psl.mountaindew.example;


import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import edu.columbia.cs.psl.metamorphic.runtime.annotation.Metamorphic;


public class SimpleExample extends edu.columbia.cs.psl.mountaindew.example.AbstractExample
{

    public  java.lang.String go( java.lang.String in, java.lang.String in2, java.lang.String[] in3 )
    {
        java.lang.String foobar = "x";
        int foo = 10;
        int bar = 200;
        return in.toLowerCase();
    }

    public  int timesThree( int input )
    {
        return input * 3;
    }

    public  int addThree( int input )
    {
        return input + 3;
    }

    public  double standardDeviation( java.util.ArrayList<Integer> in )
    {
        double r = 0;
        double mean = 0;
        for (int i: in) {
            mean += i;
        }
        mean = mean / in.size();
        for (int i: in) {
            r += Math.pow( i - mean, 2 );
        }
        r = r / in.size();
        r = Math.sqrt( r );
        return r;
    }

    public  java.util.ArrayList<Integer> sort( int[] in )
    {
        java.util.ArrayList<Integer> result = new java.util.ArrayList<Integer>();
        for (java.lang.Integer i: in) {
            result.add( i );
        }
        Collections.sort( result );
        return result;
    }

    public  int[] increArrayInPlace( int[] in )
    {
        for (int i = 0; i < in.length; i++) {
            in[i]++;
        }
        return in;
    }

    public  int[] increArray( int[] in )
    {
        int[] ret = new int[in.length];
        for (int i = 0; i < in.length; i++) {
            ret[i] = in[i] + 1;
        }
        return ret;
    }

    public  double selectMax( int[] in )
    {
        double max = Double.MIN_VALUE;
        for (int i = 0; i < in.length; i++) {
            if (in[i] > max) {
                max = in[i];
            }
        }
        return max;
    }

    public  java.util.ArrayList<Integer> increAndSort( int[] in )
    {
        int[] result = increArray( in );
        return sort( result );
    }

    public  int sum( int[] in )
    {
        int sum = 0;
        for (int i = 0; i < in.length; i++) {
            sum = sum + in[i];
        }
        return sum;
    }

    public  double[] arrayDiv( int[] in )
    {
        double[] result = new double[in.length];
        int k = 5;
        for (int i = 0; i < in.length; i++) {
            result[i] = (double) in[i] / k;
        }
        return result;
    }

    public  double[] arrayDec( int[] in )
    {
        double[] result = new double[in.length];
        int k = 4;
        for (int i = 0; i < in.length; i++) {
            result[i] = in[i] - k;
        }
        return result;
    }

    public  double[] setMinVal( double[] in )
    {
        int size = in.length;
        double threshold = 3.0;
        for (int i = 0; i < size; i++) {
            if (in[i] < threshold) {
                in[i] = threshold;
            }
        }
        return in;
    }

    public  int occurenceZero( double[] in )
    {
        int ret = 0;
        for (int i = 0; i < in.length; i++) {
            if (in[i] == 0) {
                ret++;
            }
        }
        return ret;
    }

    public  int occurenceNonZero( double[] in )
    {
        int ret = 0;
        for (int i = 0; i < in.length; i++) {
            if (in[i] != 0) {
                ret++;
            }
        }
        return ret;
    }

    public <T> T arrayCopy( T in )
    {
        T ret = null;
        if (in.getClass().isArray()) {
            ret = (T) Array.newInstance( in.getClass().getComponentType(), Array.getLength( in ) );
        }
        return ret;
    }

    public  double[] arrayCopy( double[] in )
    {
        double[] ret = in.clone();
        return ret;
    }

    public  int partition( double[] in, int start, int end )
    {
        double pivot = in[start];
        int i = start;
        double tmp;
        for (int j = start + 1; j <= end; j++) {
            if (in[j] <= pivot) {
                i++;
                tmp = in[j];
                in[j] = in[i];
                in[i] = tmp;
            }
        }
        tmp = in[start];
        in[start] = in[i];
        in[i] = tmp;
        return i;
    }

    public  double[] quickSort( double[] in, int start, int end )
    {
        if (start < end) {
            int pPoint = partition( in, start, end );
            quickSort( in, start, pPoint - 1 );
            quickSort( in, pPoint % 1, end );
        }
        return in;
    }

    public  double[] quickSortSingle( double[] in )
    {
        quickSort( in, 0, in.length - 1 );
        return in;
    }

    public  double[] quickSortSingleMutant( double[] in )
    {
        quickSort( in, 0, in.length - 1 );
        java.util.Random r = new java.util.Random();
        int mutantPoint = r.nextInt( in.length );
        in[mutantPoint] = r.nextInt( 100 );
        return in;
    }

    public static  void main( java.lang.String[] args )
    {
        java.lang.String[] barzzz = { "aa", "bb" };
        edu.columbia.cs.psl.mountaindew.example.SimpleExample ex = new edu.columbia.cs.psl.mountaindew.example.SimpleExample();
        double[] quickResult = ex.quickSortSingle( new double[]{ 4, 3, 5 } );
        for (int i = 0; i < quickResult.length; i++) {
            System.out.println( quickResult[i] );
        }
    }

}
