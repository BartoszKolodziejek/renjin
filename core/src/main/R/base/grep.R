#  File src/library/base/R/grep.R
#  Part of the R package, http://www.R-project.org
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  A copy of the GNU General Public License is available at
#  http://www.r-project.org/Licenses/

strsplit <-
function(x, split, fixed = FALSE, perl = FALSE, useBytes = FALSE)
    .Internal(strsplit(x, as.character(split), fixed, perl, useBytes))

grep <-
function(pattern, x, ignore.case = FALSE, perl = FALSE,
         value = FALSE, fixed = FALSE, useBytes = FALSE, invert = FALSE)
{
    ## when value = TRUE we return names
    if(!is.character(x)) x <- structure(as.character(x), names=names(x))
    .Internal(grep(as.character(pattern), x, ignore.case, value,
                   perl, fixed, useBytes, invert))
}

grepl <-
function(pattern, x, ignore.case = FALSE, perl = FALSE,
         fixed = FALSE, useBytes = FALSE)
{
    if(!is.character(x)) x <- as.character(x)
    .Internal(grepl(as.character(pattern), x, ignore.case, FALSE,
                    perl, fixed, useBytes, FALSE))
}

sub <-
function(pattern, replacement, x, ignore.case = FALSE,
         perl = FALSE, fixed = FALSE, useBytes = FALSE)
{
    if (!is.character(x)) x <- as.character(x)
     .Internal(sub(as.character(pattern), as.character(replacement), x,
                  ignore.case, perl, fixed, useBytes))
}

gsub <-
function(pattern, replacement, x, ignore.case = FALSE,
         perl = FALSE, fixed = FALSE, useBytes = FALSE)
{
    if (!is.character(x)) x <- as.character(x)
    .Internal(gsub(as.character(pattern), as.character(replacement), x,
                   ignore.case, perl, fixed, useBytes))
}

regexpr <-
function(pattern, text, ignore.case = FALSE, perl = FALSE,
         fixed = FALSE, useBytes = FALSE)
{
    if (!is.character(text)) text <- as.character(text)
    .Internal(regexpr(as.character(pattern), text,
                      ignore.case, perl, fixed, useBytes))
}

gregexpr <-
function(pattern, text, ignore.case = FALSE, perl = FALSE,
         fixed = FALSE, useBytes = FALSE)
{
    if (!is.character(text)) text <- as.character(text)
    .Internal(gregexpr(as.character(pattern), text,
                       ignore.case, perl, fixed, useBytes))
}

grepRaw <-
function(pattern, x, offset = 1L, ignore.case = FALSE, value = FALSE,
         fixed = FALSE, all = FALSE, invert = FALSE)
{
    if (!is.raw(pattern)) pattern <- charToRaw(as.character(pattern))
    if (!is.raw(x)) x <- charToRaw(as.character(x))
    .Internal(grepRaw(pattern, x, offset, ignore.case, fixed, value, all, invert))
}

regexec <-
function (pattern, text, ignore.case = FALSE, perl = FALSE, fixed = FALSE,
    useBytes = FALSE)
{
    if (!is.character(text))
        text <- as.character(text)
    if (!perl || fixed)
        return(.Internal(regexec(as.character(pattern), text,
            ignore.case, fixed, useBytes)))
    match_data_from_pos_and_len <- function(pos, len) {
        attr(pos, "match.length") <- len
        pos
    }
    m <- regexpr(pattern, text, ignore.case = ignore.case, useBytes = useBytes,
        perl = TRUE)
    y <- vector("list", length(text))
    ind <- (m == -1L)
    if (any(ind)) {
        y[ind] <- rep.int(list(match_data_from_pos_and_len(-1L,
            -1L)), sum(ind))
    }
    ind <- !ind
    if (any(ind)) {
        pos <- cbind(m[ind], attr(m, "capture.start")[ind, ,
            drop = FALSE])
        len <- cbind(attr(m, "match.length")[ind], attr(m, "capture.length")[ind,
            , drop = FALSE])
        y[ind] <- Map(match_data_from_pos_and_len, split(pos,
            row(pos)), split(len, row(len)))
    }
    if (identical(attr(m, "useBytes"), TRUE))
        y <- lapply(y, `attr<-`, "useBytes", TRUE)
    y
}


agrep <-
function(pattern, x, max.distance = 0.1, costs = NULL,
         ignore.case = FALSE, value = FALSE, fixed = TRUE,
         useBytes = FALSE)
{
    pattern <- as.character(pattern)
    if(!is.character(x))
        x <- as.character(x)

    ## TRE needs integer costs: coerce here for simplicity.
    costs <- as.integer(.amatch_costs(costs))
    bounds <- .amatch_bounds(max.distance)

    .Internal(agrep(pattern, x, ignore.case, value, costs, bounds,
                    useBytes, fixed))
}

.amatch_bounds <-
function(x = 0.1)
{
    ## Expand max match distance argument for agrep() et al into bounds
    ## for the TRE regaparams struct.

    ## Note that TRE allows for possibly different (integer) costs for
    ## insertions, deletions and substitions, and allows for specifying
    ## separate bounds for these numbers as well as the total number of
    ## "errors" (transformations) and the total cost.
    ##
    ## When using unit costs (and older versions of agrep() did not
    ## allow otherwise), the total number of errors is the same as the
    ## total cost, and bounds on the total number of errors imply the
    ## same bounds for the individual transformation counts.  This no
    ## longer holds when using possibly different costs.
    ##
    ## See ? agrep for details on handling the match distance argument.
    ##
    ## Older versions of agrep() expanded fractions (of the pattern
    ## length) in R code: but as the C code determines whether matching
    ## used bytes or characters, only the C code can determine the
    ## pattern length and hence expand fractions.
    ##
    ## Unspecified bounds are taken as NA_real_, and set to INT_MAX by
    ## the C code.
    
    if(!is.list(x)) {
        ## Sanity checks.
        if(!is.numeric(x) || (x < 0))
            stop("match distance components must be non-negative")
        bounds <- c(as.double(x), rep.int(NA_real_, 4L))
    } else {
        table <-
            c("cost", "insertions", "deletions", "substitutions", "all")
        ## Partial matching.
        pos <- pmatch(names(x), table)
        if(any(is.na(pos))) {
            warning("unknown match distance components ignored")
            x <- x[!is.na(pos)]
        }
        names(x) <- table[pos]
        ## Sanity checks.
        x <- unlist(x)
        if(!all(is.numeric(x)) || any(x < 0))
            stop("match distance components must be non-negative")
        ## Defaults.
        if(!is.na(x["cost"])) {
            bounds <- rep.int(NA_real_, 5L)
        } else {
            ## If 'cost' is missing: if 'all' is missing it is set to
            ## 0.1, and the other transformation number bounds default
            ## to 'all'.
            if(is.na(x["all"]))
                x["all"] <- 0.1
            bounds <- c(NA_real_, rep.int(x["all"], 4L))
        }
        names(bounds) <- table
        bounds[names(x)] <- x
    }

    bounds
}

.amatch_costs <-
function(x = NULL)
{
    costs <- c(insertions = 1, deletions = 1, substitutions = 1)
    if(!is.null(x)) {
        x <- as.list(x)
        ## Partial matching.
        pos <- pmatch(names(x), names(costs))
        if(any(is.na(pos))) {
            warning("unknown cost components ignored")
            x <- x[!is.na(pos)]
        }
        ## Sanity checks.
        x <- unlist(x)
        if(!all(is.numeric(x)) || any(x < 0))
            stop("cost components must be non-negative")
        costs[pos] <- x
    }
    
    costs
}

regmatches <-
function(x, m, invert = FALSE)
{
    if(length(x) != length(m))
        stop(gettextf("%s and %s must have the same length",
                      sQuote("x"), sQuote("m")),
             domain = NA)

    ili <- is.list(m)

    ## Handle useBytes/encoding issues.
    ## For regexpr() and gregexpr(), we get useBytes as TRUE if useBytes
    ## was given as TRUE, or all character string involved were ASCII.
    ## Hence, if useBytes is TRUE, we need to convert non-ASCII strings
    ## to a bytes encoding before computing match substrings.
    useBytes <- if(ili)
        any(unlist(lapply(m, attr, "useBytes")))
    else
        any(attr(m, "useBytes"))
    if(useBytes) {
        ## Cf. tools::showNonASCII():
        asc <- iconv(x, "latin1", "ASCII")
        ind <- is.na(asc) | (asc != x)
        ## Alternatively, could do as in tools:::.is_ASCII().
        if(any(ind))
            Encoding(x[ind]) <- "bytes"
    }

    ## What should we do about NA matches (from matching a non-NA
    ## pattern on an NA string)?  For now, let us always "drop" them so
    ## that extracting direct and inverse matches always gives nothing.

    if(!ili && !invert) {
        so <- m[ind <- (!is.na(m) & (m > -1L))]
        eo <- so + attr(m, "match.length")[ind] - 1L
        return(substring(x[ind], so, eo))
    }

    y <- if(invert) {
        Map(function(u, so, ml) {
            if((n <- length(so)) == 1L) {
                if(is.na(so))
                    return(character())
                else if(so == -1L)
                    return(u)
            }
            beg <- if(n > 1L) {
                ## regexec() could give overlapping matches.
                ## Matches are non-overlapping iff
                ##   eo[i] < so[i + 1], i = 1, ..., n - 1.
                eo <- so + ml - 1L
                if(any(eo[-n] >= so[-1L]))
                    stop(gettextf("need non-overlapping matches for %s",
                                  sQuote("invert = TRUE")),
                         domain = NA)
                c(1L, eo + 1L)
            } else {
                c(1L, so + ml)
            }
            end <- c(so - 1L, nchar(u))
            substring(u, beg, end)
        },
            x, m,
            if(ili)
            lapply(m, attr, "match.length")
            else
            attr(m, "match.length"),
            USE.NAMES = FALSE)
    } else {
        Map(function(u, so, ml) {
            if(length(so) == 1L) {
                if(is.na(so) || (so == -1L))
                    return(character())
            }
            substring(u, so, so + ml - 1L)
        },
            x, m,
            lapply(m, attr, "match.length"),
            USE.NAMES = FALSE)
    }

    names(y) <- names(x)
    y
}

`regmatches<-` <-
function(x, m, invert = FALSE, value)
{
    if(!length(x)) return(x)
    
    y <- regmatches(x, m, !invert)

    ili <- is.list(m)

    if(!ili && !invert) {
        ## For non-list m and invert = FALSE, we need a character vector
        ## of replacement values with length the number of matched
        ## elements.
        value <- as.character(value)
        if(any(is.na(value)))
            stop("missing replacement values are not allowed")
        ## Entries for matched elements have length 2.
        pos <- which(sapply(y, length) == 2L)
        np <- length(pos)
        nv <- length(value)
        if(np != nv) {
            if(!nv)
                stop("must have replacement values for matches")
            value <- rep(value, length.out = np)
        }
        x[pos] <- paste(sapply(y, `[`, 1L),
                        value,
                        sapply(y, `[`, 2L),
                        sep = "")
        return(x)
    }

    ## We need a list of character vectors without missings, which has
    ## the same length as x.
    value <- lapply(value, as.character)
    if(any(is.na(unlist(value))))
        stop("missing replacement values are not allowed")
    if(!length(value))
        stop("value does not provide any replacement values")
    value <- rep(value, length.out = length(x))

    y <- if(invert) {
        ## Replace non-matches.
        ## An element of x with k matches has a corresponding y element
        ## of length k, and needs k + 1 replacement values.
        Map(function(u, v) {
            nu <- length(u)
            nv <- length(v)
            if(nv != (nu + 1L)) {
                if(!nv)
                    stop("must have replacements for non-matches")
                v <- rep(v, length.out = nu + 1L)
            }
            paste(v, c(u, ""), sep = "", collapse = "")
        },
            y, value, USE.NAMES = FALSE)
    } else {
        ## Replace matches.
        ## An element of x with k matches has a corresponding y element
        ## of length k + 1, and needs k replacement values.
        Map(function(u, v) {
            nu <- length(u)
            nv <- length(v)
            if(nv != (nu - 1L)) {
                if(!nv)
                    stop("must have replacements for matches")
                v <- rep(v, length.out = nu - 1L)
            }
            paste(u, c(v, ""), sep = "", collapse = "")
        },
            y, value, USE.NAMES = FALSE)
    }

    y <- unlist(y)
    names(y) <- names(x)

    y
}
