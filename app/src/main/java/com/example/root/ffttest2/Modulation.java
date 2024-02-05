package com.example.root.ffttest2;

public class Modulation {
    public static short[] pskdemod_test(double[][] fft_spectrum, int[] valid_carrier) {
        short[] out = new short[valid_carrier.length];
        for (int i = 0; i < out.length; i++) {
            out[i]=1;
        }
        return out;
    }

    /**
     * the demodulation part **/
    public static double[][] pskmod(short[] bits) {
        int bit_num = bits.length;
        double[][] mod_dat = new double[2][bit_num];
        for(int i = 0; i < bit_num; ++i){
            if(bits[i] == 0) {
                mod_dat[0][i] = 1; // real
                mod_dat[1][i] = 0; // real
            }
            else {
                mod_dat[0][i] = -1; // real
                mod_dat[1][i] = 0; // real
            }
        }
        return mod_dat;
    }


    public static double[][] qpskmod(short[] bits) {
        int bit_num = bits.length / 2;
        int remainingBit = bits.length % 2; // Check if there's an odd number of bits

        // Initialize the size of the modulated data considering the remaining bit
        int dataSize = remainingBit == 0 ? bit_num : bit_num + 1;
        double[][] mod_dat = new double[2][dataSize];

        for (int i = 0; i < bit_num; i++) {
            int bit1 = bits[2 * i];
            int bit2 = bits[2 * i + 1];

            if (bit1 == 0 && bit2 == 0) {
                mod_dat[0][i] = 1; // Real part
                mod_dat[1][i] = 1; // Imaginary part
            } else if (bit1 == 0 && bit2 == 1) {
                mod_dat[0][i ] = -1; // Real part
                mod_dat[1][i ] = 1; // Imaginary part
            } else if (bit1 == 1 && bit2 == 0) {
                mod_dat[0][i ] = 1; // Real part
                mod_dat[1][i ] = -1; // Imaginary part
            } else if (bit1 == 1 && bit2 == 1) {
                mod_dat[0][i ] = -1; // Real part
                mod_dat[1][i ] = -1; // Imaginary part
            }
        }

        // Handle the last single bit if there is one
        if (remainingBit == 1) {
            mod_dat[0][dataSize - 1] = bits[bits.length - 1] == 0 ? 1 : -1;
            mod_dat[1][dataSize - 1] = 0; // Set the imaginary part to 0
        }

        return mod_dat;
    }


    public static short[] pskdemod(double[][] fft_spectrum, int [] valid_subcarrier) {
        if (fft_spectrum[0].length == (Constants.subcarrier_number_chanest)) {
            int[] valid_subcarrer2 = new int[valid_subcarrier.length];
            for (int i = 0; i < valid_subcarrer2.length; i++) {
                valid_subcarrer2[i] = valid_subcarrier[i]-Constants.nbin1_chanest;
            }
            return pskdemod_helper(fft_spectrum, valid_subcarrer2);
        }
        else {
            return pskdemod_helper(fft_spectrum, valid_subcarrier);
        }
    }

    public static double[] phase(double[][] vals) {
        double[] out = new double[vals[0].length];
        for (int i = 0; i < out.length; i++) {
            out[i] = Math.atan2(vals[1][i], vals[0][i]);
        }
        return out;
    }

    // number of symbols / real/imaginary / data bits
    public static short[][] pskdemod_differential(double[][][] symbols, int[] valid_bins) {
        int numbins = valid_bins[1]-valid_bins[0]+1;
        // num symbols / num bins
        short[][] bits = new short[symbols.length-1][numbins];
        for (int i = 0; i < symbols.length-1; i++) {
            double[][] symbol1 = symbols[i+1];
            double[][] symbol2 = symbols[i];
            double[][] divval = Utils.dividenative(symbol1,symbol2);
            double[] phase = phase(divval);

            int counter = 0;
            for (int j = valid_bins[0]; j < valid_bins[1]; j++) {
                boolean b1 = phase[j] >= Math.PI/2;
                boolean b2 = phase[j] <= -Math.PI/2;
                if (b1|b2) {
                    bits[i][counter] = 1;
                }
                counter+=1;
            }
        }
        return bits;
    }

    public static short[][] qpskDemodulateDifferential(double[][][] symbols, int[] valid_bins) {
        int numbins = valid_bins[1] - valid_bins[0] + 1;
        int numSymbols = symbols.length - 1; // The number of symbols

        short[][] bits = new short[numSymbols][numbins * 2]; // QPSK has twice as many bits as BPSK

        for (int i = 0; i < numSymbols; i++) {
            double[][] symbol1 = symbols[i + 1];
            double[][] symbol2 = symbols[i];
            double[][] divVal = Utils.dividenative(symbol1,symbol2);

            double[] phase = phase(divVal);

            int counter = 0;
            for (int j = valid_bins[0]; j <= valid_bins[1]; j++) {
                boolean b1 = phase[j] >= -Math.PI / 4 && phase[j] < Math.PI / 4;
                boolean b2 = phase[j] >= Math.PI / 4 && phase[j] < 3 * Math.PI / 4;
                boolean b3 = phase[j] >= -3 * Math.PI / 4 && phase[j] < -Math.PI / 4;
                boolean b4 = phase[j] >= 3 * Math.PI / 4 || phase[j] < -3 * Math.PI / 4;

                if (b1) {
                    bits[i][counter] = 0;
                    bits[i][counter + 1] = 0;
                } else if (b2) {
                    bits[i][counter] = 0;
                    bits[i][counter + 1] = 1;
                } else if (b3) {
                    bits[i][counter] = 1;
                    bits[i][counter + 1] = 0;
                } else if (b4) {
                    bits[i][counter] = 1;
                    bits[i][counter + 1] = 1;
                }

                counter += 2;
            }
        }

        return bits;
    }

    public static short[] pskdemod_helper(double[][] fft_spectrum, int [] valid_subcarrier) {
        int num_valid = valid_subcarrier.length;
        short[] bit_decode = new short[num_valid];

        for(int i = 0; i< num_valid; ++i){
            int bin_index = valid_subcarrier[i];
            try {
                if (fft_spectrum[0][bin_index] > 0) {
                    bit_decode[i] = 0;
                } else {
                    bit_decode[i] = 1;
                }
            }
            catch(Exception e) {

            }
        }
        return bit_decode;
    }

    public double[][] dpskmod(short[] bits) {
        return null;
    }

    public short[] dpskdemod(double[] fft_spectrum, double f1, double f2) {
        return null;
    }
}
