# Operators

<table>
    <thead>
        <tr>
            <th colspan="2">Table of operator precedence</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><b>Operator</b></td>
            <td><b>Description</b></td>
        </tr>
        <tr>
            <td>()</td>
            <td>change operators' precedence</td>
        </tr>
        <tr>
            <td rowspan="2">[], .</td>
            <td>takes element from array or string</td>
        </tr>
        <tr>
            <td>gets property of class</td>
        </tr>
        <tr>
            <td>%</td>
            <td>modulus</td>
        </tr>
        <tr>
            <td rowspan="2">*, /</td>
            <td>multiplication</td>
        </tr>
        <tr>
            <td>division</td>
        </tr>
        <tr>
            <td rowspan="2">+,-</td>
            <td>adds two numbers or element to array or concatenates two strings</td>
        </tr>
        <tr>
            <td>arithmetic minus</td>
        </tr>
        <tr>
            <td><, >, ==, !=, <=, >=</td>
            <td>comparison operators, return 1 if true, 0 if false</td>
        </tr>
         <tr>
            <td rowspan="2">&, &#124;</td>
            <td>logic "and"</td>
        </tr>
         <tr>
            <td>logic "or"</td>
        </tr>
         <tr>
            <td rowspan="2">is, !is</td>
            <td>checks whether left operand is of right operand's type</td>
        </tr>
         <tr>
            <td>checks if lft operand is not right operand's type</td>
        </tr>
        <tr>
            <td>=</td>
            <td>assigns right operand to the left operand</td>
        </tr>
    </tbody>
</table>

## Arithmetic operators

As there are integer and double values, there are type casts. Any arithmetic operation with integer and double wil
return double as a result.