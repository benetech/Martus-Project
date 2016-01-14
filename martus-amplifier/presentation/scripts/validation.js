/*
 * Validate.js
 *
 * Author: Daniel Chu
 * Purpose: 1) To keep all our html files xhtml-compliant 2) To promote javascript code reuse
 *
 */

function validateSubmission(submittedForm)
{
        if(submittedForm.query.value == null || submittedForm.query.value == "")
        {
                alert('Please enter a query.');
                return false;
        }
        else
                return true;
}

