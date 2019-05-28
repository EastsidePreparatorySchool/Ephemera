/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eastsideprep.spacecritters.orbit;

import org.eastsideprep.spacecritters.alieninterfaces.Vector2;

/**
 *
 * @author https://www.projectpluto.com/kepler.htm
 */
public class KeplerAlgorithms {


    /* If the eccentricity is very close to parabolic,  and the eccentric
anomaly is quite low,  you can get an unfortunate situation where
roundoff error keeps you from converging.  Consider the just-barely-
elliptical case,  where in Kepler's equation,

M = E - e sin( E)

   E and e sin( E) can be almost identical quantities.  To
around this,  near_parabolic( ) computes E - e sin( E) by expanding
the sine function as a power series:

E - e sin( E) = E - e( E - E^3/3! + E^5/5! - ...)
= (1-e)E + e( -E^3/3! + E^5/5! - ...)

   It's a little bit expensive to do this,  and you only need do it
quite rarely.  (I only encountered the problem because I had orbits
that were supposed to be 'pure parabolic',  but due to roundoff,
they had e = 1+/- epsilon,  with epsilon _very_ small.)  So 'near_parabolic'
is only called if we've gone seven iterations without converging. */
    static double near_parabolic(double ecc_anom, double e) {
        double anom2 = (e > 1. ? ecc_anom * ecc_anom : -ecc_anom * ecc_anom);
        double term = e * anom2 * ecc_anom / 6.;
        double rval = (1. - e) * ecc_anom - term;
        int n = 4;

        while (Math.abs(term) > 1e-15) {
            term *= anom2 / (double) (n * (n + 1));
            rval -= term;
            n += 2;
        }
        return (rval);
    }

    static int MAX_ITERATIONS = 7;
    static double THRESH = 1.e-12;
    static double MIN_THRESH = 1.e-15;

    static double asinh(double x) {
        return Math.log(x + Math.sqrt(x * x + 1.0));
    }

    static double kepler(double ecc, double mean_anom) {
        double curr, err, thresh;
        double delta_curr = 1.;
        boolean is_negative = false;
        int n_iter = 0;

        if (mean_anom == 0.0) {
            return (0.0);
        }

        if (ecc < 1.) {
            mean_anom = Vector2.normalizeAngle(mean_anom);

            if (ecc < .99999) /* low-eccentricity formula from Meeus,  p. 195 */ {
                curr = Math.atan2(Math.sin(mean_anom), Math.cos(mean_anom) - ecc);
                do {
                    err = (curr - ecc * Math.sin(curr) - mean_anom) / (1. - ecc * Math.cos(curr));
                    curr -= err;
                } while (Math.abs(err) > THRESH);
                return (curr);
            }
        }

        if (mean_anom < 0.) {
            mean_anom = -mean_anom;
            is_negative = true;
        }

        curr = mean_anom;
        thresh = THRESH * Math.abs(1. - ecc);
        /* Due to roundoff error,  there's no way we can hope to */
 /* get below a certain minimum threshhold anyway:        */
        if (thresh < MIN_THRESH) {
            thresh = MIN_THRESH;
        }
        if (thresh > THRESH) /* i.e.,  ecc > 2. */ {
            thresh = THRESH;
        }
        if (mean_anom < Math.PI / 3. || ecc > 1.) /* up to 60 degrees */ {
            double trial = mean_anom / Math.abs(1. - ecc);

            if (trial * trial > 6. * Math.abs(1. - ecc)) /* cubic term is dominant */ {
                if (mean_anom < Math.PI) {
                    trial = Math.pow(6. * mean_anom, 1 / 3.);
                } else /* hyperbolic w/ 5th & higher-order terms predominant */ {
                    trial = asinh(mean_anom / ecc);
                }
            }
            curr = trial;
        }
        if (ecc < 1.) {
            while (Math.abs(delta_curr) > thresh) {
                if (n_iter++ > MAX_ITERATIONS) {
                    err = near_parabolic(curr, ecc) - mean_anom;
                } else {
                    err = curr - ecc * Math.sin(curr) - mean_anom;
                }
                delta_curr = -err / (1. - ecc * Math.cos(curr));
                curr += delta_curr;
            }
        } else {
            while (Math.abs(delta_curr) > thresh) {
                if (n_iter++ > MAX_ITERATIONS) {
                    err = -near_parabolic(curr, ecc) - mean_anom;
                } else {
                    err = ecc * Math.sinh(curr) - curr - mean_anom;
                }
                delta_curr = -err / (ecc * Math.cosh(curr) - 1.);
                curr += delta_curr;
            }
        }
        return (is_negative ?  - curr :  curr);
    }

}
