#!/bin/bash

# The Martus(tm) free, social justice documentation and
# monitoring software. Copyright (C) 2007-2012, Beneficent
# Technology, Inc. (Benetech).
# 
# Martus is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either
# version 2 of the License, or (at your option) any later
# version with the additions and exceptions described in the
# accompanying Martus license file entitled "license.txt".
# 
# It is distributed WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, including warranties of fitness of purpose or
# merchantability.  See the accompanying Martus License and
# GPL license for more details on the required license terms
# for this software.
# 
# You should have received a copy of the GNU General Public
# License along with this program; if not, write to the Free
# Software Foundation, Inc., 59 Temple Place - Suite 330,
# Boston, MA 02111-1307, USA.

# ----------------------------------------------------------------------------
# routines to augment path-style variables
# ----------------------------------------------------------------------------

# $1 is path variable name, other args are dirs; append dirs one by one
append2path() {
	local do_reverse_dirs=
	[[ $1 == -r ]] && { do_reverse_dirs=1; shift; }
	local pathname=$1; shift
	local path=${!pathname}

	local dirs=$* dir
	[[ $do_reverse_dirs ]] &&
	for dir
	    do	dirs="$dir $dirs"
	done
	for dir in $dirs
	    do  case $pathname in
		    MANPATH ) [[ -L $dir ]] && continue ;;
		esac
		case :$path: in
		   *:$dir:* ) ;;
		   * ) [[ -d $dir ]] || continue
		       [[ -n $path ]] && path=$path:$dir || path=$dir
		       ;;
		esac
	done

	eval "$pathname=\$path"
}

# ----------------------------------------------------------------------------

# $1 is path variable name, other args are dirs; prepend dirs one by one
prepend2path() {
	local do_reverse_dirs=
	[[ $1 == -r ]] && { do_reverse_dirs=1; shift; }
	local pathname=$1; shift
	local path=${!pathname}

	local dirs=$* dir
	[[ $do_reverse_dirs ]] &&
	for dir
	    do	dirs="$dir $dirs"
	done
	for dir in $dirs
	    do  case $pathname in
		    MANPATH ) [[ -L $dir ]] && continue ;;
		esac
		case :$path: in
		   *:$dir:* ) ;;
		   * ) [[ -d $dir ]] || continue
		       [[ -n $path ]] && path=$dir:$path || path=$dir
		       ;;
		esac
	done

	eval "$pathname=\$path"
}

true					# must exit with 0
