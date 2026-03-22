import React from "react";

const SearchSuggestions = ({ value, onChange, placeholder }) => {
    return (
        <input
            type="text"
            className="filter-input"
            placeholder={placeholder || "Search services"}
            value={value}
            onChange={(e) => onChange(e.target.value)}
        />
    );
};

export default SearchSuggestions;
